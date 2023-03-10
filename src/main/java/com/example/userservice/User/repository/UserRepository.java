package com.example.userservice.User.repository;

import com.example.userservice.User.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    UserEntity findByUserId(String userId);

    UserEntity findByEmail(String Email);
}
