package com.example.fastrail.controller;

import com.example.fastrail.dto.UsersDTO;
import com.example.fastrail.model.Users;
import com.example.fastrail.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @GetMapping
    public String method(){
        return "Hi User!!";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UsersDTO usersDTO){
        Users users = usersService.registerUser(usersDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(users);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UsersDTO usersDTO){
        String token = usersService.checkLogin(usersDTO);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }
}
