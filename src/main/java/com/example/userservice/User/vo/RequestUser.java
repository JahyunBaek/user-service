package com.example.userservice.User.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RequestUser {
    @NotNull(message="Email cannot be null")
    @Size(min=2,message = "Email not be less than two characters.")
    @Email
    private String email;

    @NotNull(message="pwd cannot be null")
    @Size(min=8,max=16,message = "pwd not be grater than eight characters and less than 16 characters")
    private String pwd;

    @NotNull(message="name cannot be null")
    @Size(min=2,message = "name not be less than two characters.")
    private String name;
}
