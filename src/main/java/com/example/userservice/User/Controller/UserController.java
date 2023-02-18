package com.example.userservice.User.Controller;

import com.example.userservice.User.Data.Greeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j
@RequestMapping("/")
public class UserController {

    private Greeting greeting;

    @GetMapping("/health_check")
    public String health_check(){
        return "health_check";
    }

    @GetMapping("/welcome")
    public String welcome(){
        return greeting.getMessage();
    }
}
