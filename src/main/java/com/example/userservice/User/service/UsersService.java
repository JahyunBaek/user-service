package com.example.userservice.User.service;

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
}
