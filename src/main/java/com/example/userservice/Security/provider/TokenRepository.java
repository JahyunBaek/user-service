package com.example.userservice.Security.provider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Repository;


public interface TokenRepository extends JpaRepository<Token, Long> {
}
