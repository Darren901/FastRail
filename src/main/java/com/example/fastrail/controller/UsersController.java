package com.example.fastrail.controller;

import com.example.fastrail.dto.UsersDTO;
import com.example.fastrail.model.Users;
import com.example.fastrail.repository.UsersRepository;
import com.example.fastrail.service.OtpService;
import com.example.fastrail.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private OtpService otpService;

    @GetMapping
    public String method(){
        return "Hi User!!";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UsersDTO usersDTO){
        if(!otpService.validateOTP(usersDTO.getEmail(), usersDTO.getOtp())){
            return ResponseEntity.badRequest().body(Map.of("message", "驗證碼錯誤或已經過期"));
        }

        Users users = usersService.registerUser(usersDTO);
        otpService.deleteOTP(usersDTO.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(users);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UsersDTO usersDTO){
        Map<String, Object> map = usersService.checkLogin(usersDTO);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request){
        String email = request.get("email");

        if(usersService.existsByEmail(email)){
            return ResponseEntity.badRequest().body(Map.of("message", "此電子郵件已註冊"));
        }

        String otp = otpService.generationOTP(email);
        otpService.sendOTPAsync(email, otp);

        return ResponseEntity.ok(Map.of("message", "驗證碼已發送到您的郵箱"));
    }
}
