package com.example.fastrail.service;

import com.example.fastrail.dto.UsersDTO;
import com.example.fastrail.model.Users;
import com.example.fastrail.repository.UsersRepository;
import com.example.fastrail.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepo;
    private final JwtUtil jwtUtil;

    public Users registerUser(UsersDTO usersDTO){
        if(usersRepo.existsByEmail(usersDTO.getEmail())){
            throw new RuntimeException("Email不能重複");
        }
        if(usersRepo.existsByTwId(usersDTO.getTwId())){
            throw new RuntimeException("此身分證已經註冊過");
        }

        Users user = new Users();
        user.setEmail(usersDTO.getEmail());
        user.setName(usersDTO.getName());
        if(usersDTO.getPassword() != null && !usersDTO.getPassword().isEmpty()){
            String encodePwd = passwordEncoder.encode(usersDTO.getPassword());
            user.setPassword(encodePwd);
        }
        user.setPhone(usersDTO.getPhone());
        user.setTwId(usersDTO.getTwId());

        return usersRepo.save(user);
    }

    public Map<String, Object> checkLogin(UsersDTO usersDTO){
        Users user = usersRepo.findByEmail(usersDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("找不到此帳號"));

        boolean result = passwordEncoder.matches(usersDTO.getPassword(), user.getPassword());

        if(!result){
            throw new RuntimeException("密碼錯誤");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("token", jwtUtil.generateToken(user));
        map.put("userId", user.getId());

        return map;
    }

    public Map<String, Object> loginWithOauth(String email){
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("找不到此帳號"));

        Map<String, Object> map = new HashMap<>();
        map.put("token", jwtUtil.generateToken(user));
        map.put("userId", user.getId());

        return map;
    }

    public boolean existsByEmail(String email){
        return usersRepo.existsByEmail(email);
    }
}
