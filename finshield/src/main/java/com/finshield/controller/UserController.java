package com.finshield.controller;

import com.finshield.dto.CreateUserRequest;
import com.finshield.dto.CreateUserResponse;
import com.finshield.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(@Valid @RequestBody CreateUserRequest req) {
        return userService.create(req);
    }
}