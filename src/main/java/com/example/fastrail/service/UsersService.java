package com.example.fastrail.service;

import com.example.fastrail.dto.UsersDTO;
import com.example.fastrail.model.Users;
import com.example.fastrail.repository.UsersRepository;
import com.example.fastrail.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsersService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsersRepository usersRepo;

    @Autowired
    private JwtUtil jwtUtil;

    public Users registerUser(UsersDTO usersDTO){
        if(usersRepo.existsByEmail(usersDTO.getEmail())){
            throw new RuntimeException("Email不能重複");
        }
        if(usersRepo.existsByTwId(usersDTO.getTwId())){
            throw new RuntimeException("此身分證已經註冊過");
        }

        String encodePwd = passwordEncoder.encode(usersDTO.getPassword());
        Users user = new Users();
        user.setEmail(usersDTO.getEmail());
        user.setName(usersDTO.getName());
        user.setPassword(encodePwd);
        user.setPhone(usersDTO.getPhone());
        user.setTwId(usersDTO.getTwId());

        return usersRepo.save(user);
    }

    public String checkLogin(UsersDTO usersDTO){
        Users user = usersRepo.findByEmail(usersDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("找不到此帳號"));

        boolean result = passwordEncoder.matches(usersDTO.getPassword(), user.getPassword());

        if(!result){
            throw new RuntimeException("密碼錯誤");
        }

        return jwtUtil.generateToken(user);
    }
}
