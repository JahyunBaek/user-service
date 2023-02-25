package com.example.userservice.User.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RequestLogin {
    @NotNull(message = "Email Cannot be null")
    @Size(min = 2,message = "Email not be less 2 char")
    private String email;

    @NotNull(message = "Email Cannot be null")
    @Size(min = 8,message = "Email not be less 8 char")
    private String password;
}
