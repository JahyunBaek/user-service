package com.example.userservice.User.service;

import com.example.userservice.Security.provider.JwtProvider;
import com.example.userservice.Security.provider.Token;
import com.example.userservice.Security.provider.TokenDto;
import com.example.userservice.Security.provider.TokenRepository;
import com.example.userservice.User.client.OrderServiceClient;
import com.example.userservice.User.dto.UserDto;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

import com.example.userservice.User.entity.Authority;
import com.example.userservice.User.entity.UserEntity;
import com.example.userservice.User.repository.UserRepository;
import com.example.userservice.User.vo.RequestUser;
import com.example.userservice.User.vo.ResponseOrder;
import com.example.userservice.User.vo.ResponseUser;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
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
    private final CircuitBreakerFactory circuitBreakerFactory;

    private final RetryRegistry retryRegistry;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private Integer r_exp;

    @Value("${orders-service.url}")
    private String orderUrl;

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
        //List<ResponseOrder> responseOrders = orderServiceClient.getOrders(userId);
        //userDto.setOrders(responseOrders);

        org.springframework.cloud.client.circuitbreaker.CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");

        List<ResponseOrder> responseOrders = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),Throwable -> new ArrayList<>());

        userDto.setOrders(responseOrders);

        return userDto;
    }


    @Override
    public Iterable<UserEntity> getUserByAll() {

        return userRepository.findAll();
    }

    /**
     * Refresh 토큰을 생성한다.
     * Redis 내부에는
     * refreshToken:memberId : tokenValue
     * 형태로 저장한다.
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
        Token token = tokenRepository.findById(member.getId()).orElseThrow(() -> new Exception("만료된 계정입니다. 로그인을 다시 시도하세요"));
        // 해당유저의 Refresh 토큰 만료 : Redis에 해당 유저의 토큰이 존재하지 않음
        if (token.getRefresh_token() == null) {
            return null;
        } else {
            // 리프레시 토큰 만료일자가 얼마 남지 않았을 때 만료시간 연장..?
            if(token.getExpiration() < 10) {
                token.setExpiration(1000);
                tokenRepository.save(token);
            }

            // 토큰이 같은지 비교
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

        if(member == null) throw new BadCredentialsException("잘못된 계정정보입니다.");

        Token refreshToken = validRefreshToken(member, token.getRefresh_token());

        if (refreshToken != null) {
            return TokenDto.builder()
                    .access_token(jwtProvider.createToken(email, member.getRoles()))
                    .refresh_token(refreshToken.getRefresh_token())
                    .build();
        } else {
            throw new Exception("로그인을 해주세요");
        }
    }
}
