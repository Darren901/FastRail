package com.example.fastrail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final long OTP_VALID_DURATION = 5 * 60 * 1000;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JavaMailSender javaMailSender;

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

    public void sendOTPEmail(String email, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("台灣高鐵 - 會員註冊驗證碼");

            String htmlContent =
                    "<div style='font-family: \"微軟正黑體\", Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>" +
                            "<div style='background-color: #db5009; padding: 15px; text-align: center; border-radius: 5px 5px 0 0;'>" +
                            "<h1 style='color: #333; margin: 0;'>台灣高鐵</h1>" +
                            "</div>" +
                            "<div style='padding: 20px; background-color: #f8f9fa;'>" +
                            "<p style='font-size: 16px; color: #333;'>親愛的高鐵會員，您好：</p>" +
                            "<p style='font-size: 16px; color: #333;'>感謝您註冊台灣高鐵會員服務。請使用以下驗證碼完成註冊流程：</p>" +
                            "<div style='background-color: #fff; padding: 15px; text-align: center; margin: 20px 0; border-radius: 4px; border: 2px solid #db5009;'>" +
                            "<h2 style='font-size: 28px; letter-spacing: 5px; color: #db5009; margin: 0;'>" + otp + "</h2>" +
                            "</div>" +
                            "<p style='font-size: 14px; color: #666;'>此驗證碼將在5分鐘後失效，請勿將驗證碼分享給他人。</p>" +
                            "<p style='font-size: 14px; color: #666;'>若您並未申請此驗證碼，請忽略此信件。</p>" +
                            "</div>" +
                            "<div style='padding: 15px; text-align: center; font-size: 12px; color: #666; border-top: 1px solid #e0e0e0;'>" +
                            "<p>此為系統自動發送郵件，請勿直接回覆。</p>" +
                            "<p>如有任何疑問，請致電客服專線：<span style='color: #fe0000;'>4066-3000</span>（手機請加02）</p>" +
                            "<p>&copy; " + java.time.Year.now().getValue() + " 台灣高速鐵路股份有限公司. 版權所有。</p>" +
                            "</div>" +
                            "</div>";

            helper.setText(htmlContent, true); // 第二個參數true表示使用HTML格式

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("發送OTP郵件失敗", e);
        }
    }

    public boolean validateOTP(String email, String otp){
        String storeOtp = redisTemplate.opsForValue().get("otp" + email);
        return storeOtp != null && storeOtp.equals(otp);
    }

    public void deleteOTP(String email){
        redisTemplate.delete("otp" + email);
    }
}
