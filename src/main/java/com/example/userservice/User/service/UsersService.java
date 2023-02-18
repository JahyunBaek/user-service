package com.example.userservice.User.service;

import com.example.userservice.User.dto.UserDto;
import com.example.userservice.User.entity.UserEntity;

public interface UsersService {
    UserDto createUser(UserDto user);
    UserDto getUserByUserId(String userId);
    Iterable<UserEntity> getUserByAll();
}
