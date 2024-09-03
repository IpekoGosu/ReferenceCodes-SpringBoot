package com.example.demo.domain.user.repository.impl;

import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepositoryJpa extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
