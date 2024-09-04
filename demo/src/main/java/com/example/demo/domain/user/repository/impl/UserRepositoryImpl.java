package com.example.demo.domain.user.repository.impl;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.exception.CustomException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserRepositoryJpa userRepositoryJpa;

    @Override
    public User findByUserId(long userId) {
        return userRepositoryJpa.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "user not found"));
    }

    @Override
    public Optional<User> findOptionalByEmail(String email) {
        return userRepositoryJpa.findByEmail(email);
    }

    @Override
    public User findByEmail(String email) {
        return userRepositoryJpa.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "user not found"));
    }

    @Override
    public User save(User user) {
        return userRepositoryJpa.save(user);
    }

}
