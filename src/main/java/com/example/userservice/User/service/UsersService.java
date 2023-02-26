package com.example.userservice.User.service;

import com.example.userservice.Security.provider.Token;
import com.example.userservice.Security.provider.TokenDto;
import com.example.userservice.User.dto.UserDto;
import com.example.userservice.User.entity.UserEntity;
import com.example.userservice.User.vo.RequestUser;
import com.example.userservice.User.vo.ResponseUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UsersService  {
    UserDto createUser(UserDto user);
    UserDto getUserByUserId(String userId);
    Iterable<UserEntity> getUserByAll();
    ResponseUser login(RequestUser request) throws Exception;

    String createRefreshToken(UserEntity member);
    TokenDto refreshAccessToken(TokenDto token) throws Exception;
    Token validRefreshToken(UserEntity member, String refreshToken) throws Exception;
}
