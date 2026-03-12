package com.example.pintbackend.domain.user.entity;


import com.example.pintbackend.domain.Post;
import com.example.pintbackend.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

  @Column(name = "username", nullable = false)
  private String userName;

  private String city;  // 거주지

  private String introduction;  // 소개글

  @Column(name = "profile_image_key")
  private String profileImageS3Key;

  @OneToMany(mappedBy = "user")
  private final List<Post> posts = new ArrayList<>();

  @Builder
  private User(String email, String password, String userName, String city, String introduction,
      String profileImageS3Key) {
    this.email = email;
    this.password = password;
    this.userName = userName;
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
}
