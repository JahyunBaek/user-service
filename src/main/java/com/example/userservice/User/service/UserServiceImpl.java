package com.example.userservice.User.service;

import com.example.userservice.Security.provider.JwtProvider;
import com.example.userservice.Security.provider.Token;
import com.example.userservice.Security.provider.TokenDto;
import com.example.userservice.Security.provider.TokenRepository;
import com.example.userservice.User.client.OrderServiceClient;
import com.example.userservice.User.dto.UserDto;

import java.lang.reflect.Type;
import java.util.*;

import com.example.userservice.User.entity.Authority;
import com.example.userservice.User.entity.UserEntity;
import com.example.userservice.User.repository.UserRepository;
import com.example.userservice.User.vo.RequestUser;
import com.example.userservice.User.vo.ResponseOrder;
import com.example.userservice.User.vo.ResponseUser;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@RefreshScope
public class UserServiceImpl implements UsersService {


    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final JwtProvider jwtProvider;

    private final TokenRepository tokenRepository;

    private final OrderServiceClient orderServiceClient;
    //private final RestTemplate restTemplate;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private Integer r_exp;

    @Value("${orders-service.url}")
    private String orderUrl;

    public ResponseUser login(RequestUser request) throws Exception {
        UserEntity member = userRepository.findByEmail(request.getEmail());

        if(member == null)
            throw new BadCredentialsException("????????? ?????????????????????.");

        if (!encoder.matches(request.getPwd(), member.getEncryptedPwd())) {
            throw new BadCredentialsException("????????? ?????????????????????.");
        }

        return ResponseUser.builder()
                .userId(member.getUserId())
                .name(member.getName())
                .email(member.getEmail())
                .roles(member.getRoles())
                .token(TokenDto.builder()
                        .access_token(jwtProvider.createToken(member.getEmail(), member.getRoles()))
                        .refresh_token(member.getRefreshToken())
                        .build())
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
        userEntity.setRefreshToken(createRefreshToken(userEntity));
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

        /*restTemplate*/
//        ResponseEntity<List<ResponseOrder>> responseOrders = restTemplate.exchange(String.format(orderUrl,userId),
//                HttpMethod.GET, null, new ParameterizedTypeReference<List<ResponseOrder>>() {});
//        userDto.setOrders(responseOrders.getBody());

        /*Feign client*/
        List<ResponseOrder> responseOrders = orderServiceClient.getOrders(userId);
        userDto.setOrders(responseOrders);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {

        return userRepository.findAll();
    }

    /**
     * Refresh ????????? ????????????.
     * Redis ????????????
     * refreshToken:memberId : tokenValue
     * ????????? ????????????.
     */
    public String createRefreshToken(UserEntity member) {
        Token token = tokenRepository.save(
                Token.builder()
                        .id(member.getId())
                        .refresh_token(UUID.randomUUID().toString())
                        .expiration(r_exp)
                        .build()
        );
        return token.getRefresh_token();
    }

    public Token validRefreshToken(UserEntity member, String refreshToken) throws Exception {
        Token token = tokenRepository.findById(member.getId()).orElseThrow(() -> new Exception("????????? ???????????????. ???????????? ?????? ???????????????"));
        // ??????????????? Refresh ?????? ?????? : Redis??? ?????? ????????? ????????? ???????????? ??????
        if (token.getRefresh_token() == null) {
            return null;
        } else {
            // ???????????? ?????? ??????????????? ?????? ?????? ????????? ??? ???????????? ??????..?
            if(token.getExpiration() < 10) {
                token.setExpiration(1000);
                tokenRepository.save(token);
            }

            // ????????? ????????? ??????
            if(!token.getRefresh_token().equals(refreshToken)) {
                return null;
            } else {
                return token;
            }
        }
    }

    public TokenDto refreshAccessToken(TokenDto token) throws Exception {
        String email = jwtProvider.getEmail(token.getAccess_token());
        UserEntity member = userRepository.findByEmail(email);

        if(member == null) throw new BadCredentialsException("????????? ?????????????????????.");

        Token refreshToken = validRefreshToken(member, token.getRefresh_token());

        if (refreshToken != null) {
            return TokenDto.builder()
                    .access_token(jwtProvider.createToken(email, member.getRoles()))
                    .refresh_token(refreshToken.getRefresh_token())
                    .build();
        } else {
            throw new Exception("???????????? ????????????");
        }
    }
}
