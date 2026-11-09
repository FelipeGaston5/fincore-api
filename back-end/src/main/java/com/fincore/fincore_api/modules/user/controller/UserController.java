package com.fincore.fincore_api.modules.user.controller;

import com.fincore.fincore_api.modules.user.dto.CreateUserRequest;
import com.fincore.fincore_api.modules.user.dto.UserResponse;
import com.fincore.fincore_api.modules.user.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("api/v1/users")
public class UserController{

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
        @Valid 
        @RequestBody 
        CreateUserRequest request
    ) {
        UserResponse userResponse = userService.createUser(request);

        URI location = URI.create("api/v1/users/" + userResponse.id());

        return ResponseEntity.created(location).body(userResponse);
    }
}