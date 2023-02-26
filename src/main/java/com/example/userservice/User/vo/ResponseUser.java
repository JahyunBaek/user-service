package com.example.userservice.User.vo;

import com.example.userservice.Security.provider.TokenDto;
import com.example.userservice.User.entity.Authority;
import com.example.userservice.User.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseUser {

    private String name;
    private String email;
    private String userId;
    private List<Authority> roles = new ArrayList<>();

    private TokenDto token;

    private List<ResponseOrder> orders;


    public ResponseUser(UserEntity member) {
        this.userId = member.getUserId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.roles = member.getRoles();
    }
}
