package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository {
    /**
     * user 식별번호로 찾기
     */
    User findByUserId(long userId);

    Optional<User> findOptionalByEmail(String email);

    User findByEmail(String email);

    User save(User user);
}
