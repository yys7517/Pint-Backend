# N+1 Query Problem & Optimization

## What is N+1?

N+1 is a database performance problem where your code executes **1 query** to fetch a list of N items, then **N additional queries** (one per item in a loop) to fetch related data.

For example, if you load 10 posts and then need each post's author, instead of fetching all 10 authors in one query, the code fires 10 separate queries — one for each post's author.

```
1 query to get posts  +  N queries to get each post's user  =  N+1
```

---

## The Problem in `PostService.getAllPost()`

### Entity Relationships

```
User (1) ──── (*) Post ──── (*) PostLike ──── (1) User
```

- `Post.user` is `@ManyToOne(fetch = FetchType.LAZY)` — Hibernate does NOT load the User when it loads a Post. It creates an empty **proxy** that waits until you access it.
- `Post.likes` is `@OneToMany` (lazy by default) — same idea, the list isn't loaded until you call `.size()` or iterate it.

### Example Data

**`users`**
| id | username |
|----|----------|
| 1  | alice    |
| 2  | bob      |
| 3  | charlie  |

**`posts`**
| id | user_id | description  |
|----|---------|------------- |
| 10 | 1       | sunset photo |
| 11 | 2       | my cat       |
| 12 | 1       | coffee shop  |
| 13 | 3       | mountain     |
| 14 | 2       | beach day    |

**`post_like`**
| id | post_id | user_id |
|----|---------|---------|
| 1  | 10      | 2       |
| 2  | 10      | 3       |
| 3  | 11      | 1       |
| 4  | 13      | 1       |
| 5  | 13      | 2       |
| 6  | 14      | 1       |

### What the old code did (bob opens the feed, page size = 5)

```java
// PostService.getAllPost()

Page<Post> posts = postRepository.findAll(pageable);           // Query 1

posts.getContent().stream().map(post -> {
    postLikeRepository.existsByPostIdAndUserId(post.getId(), userId);  // Query per post
    post.getUser().getUsername();                                       // Query per unique user (lazy load)
    post.getLikes().size();                                             // Query per post (lazy load)
});
```

#### Query-by-query trace:

```
Query  1: SELECT * FROM posts LIMIT 5                                   ← findAll
Query  2: SELECT 1 FROM post_like WHERE post_id=14 AND user_id=2       ← existsBy (post 14)
Query  3: SELECT * FROM users WHERE id=2                                ← lazy load bob (post 14's author)
Query  4: SELECT * FROM post_like WHERE post_id=14                      ← likes.size() (post 14)
Query  5: SELECT 1 FROM post_like WHERE post_id=13 AND user_id=2       ← existsBy (post 13)
Query  6: SELECT * FROM users WHERE id=3                                ← lazy load charlie (post 13's author)
Query  7: SELECT * FROM post_like WHERE post_id=13                      ← likes.size() (post 13)
Query  8: SELECT 1 FROM post_like WHERE post_id=12 AND user_id=2       ← existsBy (post 12)
Query  9: SELECT * FROM users WHERE id=1                                ← lazy load alice (post 12's author)
Query 10: SELECT * FROM post_like WHERE post_id=12                      ← likes.size() (post 12)
Query 11: SELECT 1 FROM post_like WHERE post_id=11 AND user_id=2       ← existsBy (post 11)
          (bob already cached — no query)                               ← hibernate cache hit
Query 12: SELECT * FROM post_like WHERE post_id=11                      ← likes.size() (post 11)
Query 13: SELECT 1 FROM post_like WHERE post_id=10 AND user_id=2       ← existsBy (post 10)
          (alice already cached — no query)                             ← hibernate cache hit
Query 14: SELECT * FROM post_like WHERE post_id=10                      ← likes.size() (post 10)
```

**Total: 14 queries for 5 posts.**

The three problems:
1. `post.getUser()` triggers a **lazy load** per unique author
2. `existsByPostIdAndUserId()` is called **once per post** in a loop
3. `post.getLikes().size()` loads the **entire likes collection** per post just to count

---

## The Solution: Batch Everything

Instead of querying one-at-a-time inside a loop, we batch all the data we need into bulk queries **before** the loop.

### Step 1 — `findAll(pageable)` (same as before)
```sql
SELECT * FROM posts ORDER BY created_at DESC LIMIT 5
```
Returns post IDs: `[14, 13, 12, 11, 10]`

### Step 2 — `findAllWithUser(ids)` (JOIN FETCH)
```sql
SELECT p.*, u.*
FROM posts p
JOIN users u ON p.user_id = u.id
WHERE p.id IN (14, 13, 12, 11, 10)
```
One query loads **all 5 posts AND their authors** together. Hibernate now has the User data in memory — `post.getUser().getUsername()` will never trigger a lazy load.

| p.id | p.description | u.id | u.username |
|------|--------------|------|------------|
| 14   | beach day    | 2    | bob        |
| 13   | mountain     | 3    | charlie    |
| 12   | coffee shop  | 1    | alice      |
| 11   | my cat       | 2    | bob        |
| 10   | sunset photo | 1    | alice      |

### Step 3 — `findLikedPostIdsByUser(ids, userId)` (batch like check)
```sql
SELECT pl.post_id
FROM post_like pl
WHERE pl.post_id IN (14, 13, 12, 11, 10)
AND pl.user_id = 2
```
Returns: `{10, 13}` — the posts bob liked.

In Java: `likedPostIds.contains(post.getId())` — instant `Set` lookup, no query.

### Step 4 — `countByPostIds(ids)` (batch like count)
```sql
SELECT pl.post_id, COUNT(pl)
FROM post_like pl
WHERE pl.post_id IN (14, 13, 12, 11, 10)
GROUP BY pl.post_id
```
Returns:

| post_id | count |
|---------|-------|
| 10      | 2     |
| 11      | 1     |
| 13      | 2     |
| 14      | 1     |

Post 12 has 0 likes (absent from results, default to 0 in Java).

### Result: 4 queries. Always. Regardless of page size.

---

## Before vs After

```
                    BEFORE              AFTER
Page size 5:        14 queries          4 queries
Page size 10:       ~31 queries         4 queries
Page size 50:       ~151 queries        4 queries
Page size 100:      ~301 queries        4 queries
```

The old count grows **linearly** with page size (`~3N + 1`).
The new count is **constant** at 4.

---

## Repository Changes

### PostRepository — added `findAllWithUser`
```java
@Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id IN :ids")
List<Post> findAllWithUser(@Param("ids") List<Long> ids);
```

### PostLikeRepository — added batch queries
```java
// Batch check: which posts did this user like?
@Query("SELECT pl.post.id FROM PostLike pl WHERE pl.post.id IN :postIds AND pl.user.id = :userId")
Set<Long> findLikedPostIdsByUser(@Param("postIds") List<Long> postIds, @Param("userId") Long userId);

// Batch count: how many likes does each post have?
@Query("SELECT pl.post.id, COUNT(pl) FROM PostLike pl WHERE pl.post.id IN :postIds GROUP BY pl.post.id")
List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);
```

---

## Key Takeaways

1. **Never call a repository method inside a `.map()` / `for` loop** — this is the #1 cause of N+1.
2. **Lazy loading is a trap in loops** — `post.getUser()` looks harmless but fires a query if the User wasn't pre-fetched.
3. **`collection.size()` loads the entire collection** — use a `COUNT` query instead.
4. **Batch before the loop** — fetch all related data you need with `IN (...)` queries, then do lookups from a `Map` or `Set` in Java.
5. **JOIN FETCH + Pageable don't mix** — use the two-step approach: paginate first for IDs, then JOIN FETCH those IDs.
