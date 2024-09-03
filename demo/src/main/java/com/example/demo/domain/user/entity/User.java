package com.example.demo.domain.user.entity;

import com.example.demo.global.base.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity(name = "USER")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Column(name = "USER_NO")
    private long userNo;

    @Column(name = "EMAIL", length = 30, nullable = false, unique = true) // varchar(30), not null, unique
    private String email;

    @Column(name = "PASSWORD", length = 300, nullable = false)
    private String password;

    // !!! Enum을 사용하기 위한 어노테이션. String / Ordinal 둘 중 하나 골라서 사용 가능. 디폴트는 ordinal (숫자)
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", length = 30)
    private UserRole role;
}

// 먼저 테이블을 만들고 JPA 연결
//CREATE TABLE user (
//	USER_NO				BIGINT			PRIMARY KEY AUTO_INCREMENT,
//    EMAIL				VARCHAR(30)		NOT NULL UNIQUE,
//    PASSWORD			VARCHAR(300)	NOT NULL,
//    ROLE				VARCHAR(10)		DEFAULT 'ROLE_USER',
//    CREATED_DATE		DATETIME		DEFAULT CURRENT_TIMESTAMP,
//    LAST_MODIFIED_DATE	DATETIME		DEFAULT CURRENT_TIMESTAMP,
//    DELETED_DATE		DATETIME		DEFAULT NULL
//);