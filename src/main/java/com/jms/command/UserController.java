package com.jms.command;

import brave.Span;
import brave.Tracer;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Tracer tracer;

    @GetMapping(path = "/create")
    public String createUser(@RequestParam( required=false ) String username){
        return userService.createUser(username);
    }

    @Transactional
    @GetMapping(path = "/transactional/create")
    @NewSpan
    public String createUserTransactional(@SpanTag("username") @RequestParam( required=false ) String username){
        return userService.createUser(username);
    }

    @NewSpan
    @Transactional
    @GetMapping(path = "/transactional/with_clone/create")
    public String createUserWithCloneTransactional(@SpanTag("username") @RequestParam( required=false ) String username){
        return userService.createUser(username + "_clone") + " : " + userService.createUser(username);
    }

    @NewSpan
    @GetMapping(path = "/all")
    public String getUsers(){
        return userService.queryUser().map(userEntity -> userEntity.getUsername()).collect(Collectors.joining(" : "));
    }
}
