package com.example.fastrail.service;

import com.example.fastrail.config.RabbitMQConfig;
import com.example.fastrail.dto.AuditPayload;
import com.example.fastrail.dto.OtpEmailPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final long OTP_VALID_DURATION = 5 * 60 * 1000;

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public String generationOTP(String email){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder otp = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            otp.append(characters.charAt(random.nextInt(characters.length())));
        }

        redisTemplate.opsForValue().set(
                "otp" + email,
                otp.toString(),
                OTP_VALID_DURATION,
                TimeUnit.MILLISECONDS
        );

        return otp.toString();
    }

    public void sendOTPAsync(String email, String otp) {
        OtpEmailPayload payload = new OtpEmailPayload(email, otp);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                payload
        );

        AuditPayload auditPayload = new AuditPayload("EMAIL_OTP", "user@example.com", "SENT", Instant.now());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.AUDIT_ROUTING_KEY,
                auditPayload
        );
    }

    public boolean validateOTP(String email, String otp){
        String storeOtp = redisTemplate.opsForValue().get("otp" + email);
        return storeOtp != null && storeOtp.equals(otp);
    }

    public void deleteOTP(String email){
        redisTemplate.delete("otp" + email);
    }
}
