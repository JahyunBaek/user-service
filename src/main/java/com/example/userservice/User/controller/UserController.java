package com.example.userservice.User.controller;

import com.example.userservice.Security.provider.TokenDto;
import com.example.userservice.User.dto.UserDto;
import com.example.userservice.User.entity.UserEntity;
import com.example.userservice.User.service.UsersService;
import com.example.userservice.User.vo.Greeting;
import com.example.userservice.User.vo.RequestUser;
import com.example.userservice.User.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
/*@RequestMapping("/user-service")*/
public class UserController {

    private final Environment environment;

    private final Greeting greeting;

    private final UsersService usersService;

    @GetMapping("/health_check")
    public String health_check(HttpServletRequest request){
        String TestStr = "health_check";
        TestStr += ", key : "+environment.getProperty("jwt.secret.key");
        TestStr += ", expired : "+environment.getProperty("jwt.access-token-validity-in-seconds");
        TestStr += ", Port : " + environment.getProperty("server.port");
        TestStr += ", Local Port : " + environment.getProperty("local.server.port");
        return TestStr;
    }

    @GetMapping("/welcome")
    public String welcome(){
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity CreateUser(@RequestBody RequestUser user){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = modelMapper.map(user,UserDto.class);
        UserDto user1 = usersService.createUser(userDto);

        ResponseUser responseUser = modelMapper.map(user1,ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>>  getUsers(){
        Iterable<UserEntity> userList = usersService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        userList.forEach(v ->{
            result.add(new ModelMapper().map(v,ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId){
        UserDto userDto = usersService.getUserByUserId(userId);
        ResponseUser responseUser = new ModelMapper().map(userDto,ResponseUser.class);
        return ResponseEntity.status(HttpStatus.OK).body(responseUser);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<ResponseUser> login(@RequestBody RequestUser request) throws Exception {
        return new ResponseEntity<>(usersService.login(request), HttpStatus.OK);
    }

    @GetMapping("/refresh")
    public ResponseEntity<TokenDto> refresh(@RequestBody TokenDto token) throws Exception {
        return new ResponseEntity<>( usersService.refreshAccessToken(token), HttpStatus.OK);
    }
}
