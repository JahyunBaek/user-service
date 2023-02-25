package com.example.userservice.User.service;

import com.example.userservice.User.dto.UserDto;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.example.userservice.User.entity.UserEntity;
import com.example.userservice.User.repository.UserRepository;
import com.example.userservice.User.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UsersService {


    private final UserRepository userRepository;

    private final BCryptPasswordEncoder encoder;

    @Override
    public UserDto createUser(UserDto user) {
        user.setUserId(UUID.randomUUID().toString());
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = modelMapper.map(user,UserEntity.class);
        userEntity.setEncryptedPwd(encoder.encode(user.getPwd()));
        UserEntity save = userRepository.save(userEntity);
        return modelMapper.map(save,UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if(userEntity == null)
            throw new UsernameNotFoundException("user not found");

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = modelMapper.map(userEntity, UserDto.class);

        List<ResponseOrder> responseOrders = new ArrayList<>();
        userDto.setOrders(responseOrders);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {

        return userRepository.findAll();
    }

}
