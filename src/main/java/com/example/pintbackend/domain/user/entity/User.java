package com.example.pintbackend.domain.user.entity;


import com.example.pintbackend.domain.PostLike;
import com.example.pintbackend.domain.post.Post;
import com.example.pintbackend.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String username;

  private String city;  // 거주지

  private String introduction;  // 소개글

  @Column(name = "profile_image_key")
  private String profileImageS3Key;

  @OneToMany(mappedBy = "user")
  @OrderBy("createdAt DESC")
  private final List<Post> posts = new ArrayList<>();

  @OneToMany(mappedBy = "user")
  private final List<PostLike> likes = new ArrayList<>();

  @Builder
  private User(String email, String password, String username, String city, String introduction,
      String profileImageS3Key) {
    this.email = email;
    this.password = password;
    this.username = username;
    this.city = city;
    this.introduction = introduction;
    this.profileImageS3Key = profileImageS3Key;
  }

  public void encodePassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  public void addPost(Post post) {
    if (posts.contains(post)) {
      return;
    }

    posts.add(post);
    post.assignUser(this);
  }

  public void addLike(PostLike postLike) {
    if (likes.contains(postLike)) {
      return;
    }

    likes.add(postLike);
    postLike.assignUser(this);
  }

  public void removeLike(PostLike postLike) {
    likes.remove(postLike);
    postLike.assignUser(null);
  }

  public void update(String username, String introduction, String city, String profileImageS3Key) {
    if (username != null) this.username = username;
    if (introduction != null) this.introduction = introduction;
    if (city != null) this.city = city;
    if (profileImageS3Key != null) this.profileImageS3Key = profileImageS3Key;
  }
}
