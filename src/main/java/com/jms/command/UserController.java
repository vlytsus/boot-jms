package com.jms.command;

import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping(path = "/create")
    public String createUser(@RequestParam( required=false ) String username){
        return createUser(username);
    }

    @Transactional
    @GetMapping(path = "/transactional/create")
    public String createUserTransactional(@RequestParam( required=false ) String username){
        return createUser(username);
    }

    private String createUserInternal(String username) {
        try {
            return userService.sendUser(username);
        } catch (RuntimeException ex) {
            return ex.getMessage();
        }
    }

    @GetMapping(path = "/all")
    public String getUsers(){
        return userService.queryUser().map(userEntity -> userEntity.getUsername()).collect(Collectors.joining(" : "));
    }
}
