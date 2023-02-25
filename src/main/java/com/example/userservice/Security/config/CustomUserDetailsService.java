package com.example.userservice.Security.config;

import com.example.userservice.User.entity.UserEntity;
import com.example.userservice.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String username) {
        UserEntity userEntity = userRepository.findByEmail(username);

        if(userEntity == null) new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다.");

        Collection authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(userEntity.getEmail(),userEntity.getEncryptedPwd(),
                true,true,true,true,authorities);
    }
}
