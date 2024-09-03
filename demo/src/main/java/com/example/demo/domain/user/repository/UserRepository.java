package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.User;

public interface UserRepository {
    /**
     * user 식별번호로 찾기
     */
    User findByUserId(long userId);

    User findByEmail(String email);
}
