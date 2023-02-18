package com.example.userservice.User.dto;

import com.example.userservice.User.vo.ResponseOrder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserDto {
    private String email;
    private String pwd;
    private String name;
    private String userId;
    private Date creatAt;

    private  String encryptedPwd;

    private List<ResponseOrder> orders;
}
