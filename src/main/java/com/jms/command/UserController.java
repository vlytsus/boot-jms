package com.jms.command;

import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(path = "/create")
    public String createUser(@RequestParam( required=false ) String username){
        return userService.createUser(username);
    }

    @Transactional
    @GetMapping(path = "/transactional/create")
    public String createUserTransactional(@RequestParam( required=false ) String username){
        return userService.createUser(username);
    }

    @Transactional
    @GetMapping(path = "/transactional/with_clone/create")
    public String createUserWithCloneTransactional(@RequestParam( required=false ) String username){
        return userService.createUser(username + "_clone") + " : " + userService.createUser(username);
    }

    @GetMapping(path = "/all")
    public String getUsers(){
        return userService.queryUser().map(userEntity -> userEntity.getUsername()).collect(Collectors.joining(" : "));
    }
}
