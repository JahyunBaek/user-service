package com.example.userservice.User.service;

import com.example.userservice.Security.provider.JwtProvider;
import com.example.userservice.User.dto.UserDto;

import java.lang.reflect.Type;
import java.util.*;

import com.example.userservice.User.entity.Authority;
import com.example.userservice.User.entity.UserEntity;
import com.example.userservice.User.repository.UserRepository;
import com.example.userservice.User.vo.RequestUser;
import com.example.userservice.User.vo.ResponseOrder;
import com.example.userservice.User.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UsersService {


    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final JwtProvider jwtProvider;

    public ResponseUser login(RequestUser request) throws Exception {
        UserEntity member = userRepository.findByEmail(request.getEmail());

        if(member == null)
            throw new BadCredentialsException("잘못된 계정정보입니다.");

        if (!encoder.matches(request.getPwd(), member.getEncryptedPwd())) {
            throw new BadCredentialsException("잘못된 계정정보입니다.");
        }

        return ResponseUser.builder()
                .userId(member.getUserId())
                .name(member.getName())
                .email(member.getEmail())
                .roles(member.getRoles())
                .token(jwtProvider.createToken(member.getEmail(), member.getRoles()))
                .build();

    }

    @Override
    public UserDto createUser(UserDto user) {
        user.setUserId(UUID.randomUUID().toString());
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = modelMapper.map(user,UserEntity.class);

        userEntity.setEncryptedPwd(encoder.encode(user.getPwd()));
        userEntity.setRoles(Collections.singletonList(Authority.builder().name("ROLE_USER").build()));
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
