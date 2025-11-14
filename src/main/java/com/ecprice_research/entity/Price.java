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
@Table(name = "price")
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 Product에서 가져온 가격인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // AMAZON_JP / NAVER / COUPANG / RAKUTEN
    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String productUrl;

    private int price;       // KRW/JPY 통합 가격
    private int shipping;    // 배송비
    private String currency; // KRW / JPY

    @CreationTimestamp
    private LocalDateTime createdAt;
}
