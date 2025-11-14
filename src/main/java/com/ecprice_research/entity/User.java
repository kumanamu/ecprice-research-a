package com.ecprice_research.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")   // user 예약어 충돌 방지
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 구글 OAuth2 고유 ID (sub)
    @Column(nullable = false, unique = true)
    private String oauthId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider;    // "google"

    private String name;
    private String profileImage;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
