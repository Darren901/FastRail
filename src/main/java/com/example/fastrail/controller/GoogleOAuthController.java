package com.example.fastrail.controller;

import com.example.fastrail.config.GoogleConfig;
import com.example.fastrail.dto.RegistrationCompletionDTO;
import com.example.fastrail.dto.UsersDTO;
import com.example.fastrail.service.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/google")
@RequiredArgsConstructor
public class GoogleOAuthController {

    @Value("${frontEndHost}")
    private String FRONT_HOST;

    private final GoogleConfig googleConfig;
    private final UsersService usersService;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/login")
    public ResponseEntity<?> googleLogin(){
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + googleConfig.getClientId() +
                "&response_type=code" +
                "&scope=openid%20email%20profile" +
                "&redirect_uri=" + googleConfig.getRedirectUri() +
                "&state=state";


        return ResponseEntity.ok(Map.of("redirectUrl", authUrl));
    }

    @GetMapping
    public ResponseEntity<?> googleCallBack(@RequestParam String code) throws JsonProcessingException {
        RestClient restClient = RestClient.create();

        String queryBody = UriComponentsBuilder.newInstance()
                .queryParam("code", code)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", googleConfig.getClientId())
                .queryParam("client_secret", googleConfig.getClientSecret())
                .queryParam("redirect_uri", googleConfig.getRedirectUri())
                .build()
                .getQuery();

        // 後端發送請求至指定網址 參考文件 https://developers.google.com/identity/protocols/oauth2/web-server?hl=zh-tw#httprest_3
        String credentials = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(queryBody)
                .retrieve()
                .body(String.class);

        // 解析credentials拿token
        JsonNode jsonNode = new ObjectMapper().readTree(credentials);
        String accessToken = jsonNode.get("access_token").asText();
        String idToken = jsonNode.get("id_token").asText();

        String result = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + accessToken)
                .header("Authorization", "Bearer " + idToken)
                .retrieve()
                .body(String.class);

        log.info("-------> result : {}", result);

        JsonNode jsonNode2 = new ObjectMapper().readTree(result);
        String userEmail = jsonNode2.get("email").asText();
        String userName = jsonNode2.get("name").asText();

        if(usersService.existsByEmail(userEmail)){
            Map<String, Object> loginResult = usersService.loginWithOauth(userEmail);
            String redirectUrl = FRONT_HOST + "auth-callback?token=" + loginResult.get("token") +
                    "&userId=" + loginResult.get("userId");

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }else{
            String tempToken = UUID.randomUUID().toString();

            redisTemplate.opsForHash().putAll("google_oauth:" + tempToken, Map.of("email", userEmail, "name", userName));
            redisTemplate.expire("google_oauth:" + tempToken, 10, TimeUnit.MINUTES);

            String redirectUrl = FRONT_HOST + "complete-profile?tempToken=" + tempToken +
                    "&email=" + URLEncoder.encode(userEmail, StandardCharsets.UTF_8) +
                    "&name=" + URLEncoder.encode(userName, StandardCharsets.UTF_8) +
                    "&status=NEED_MORE_INFO";

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }

    }

    @PostMapping("/complete-registration")
    public ResponseEntity<?> completeRegistration(@RequestBody RegistrationCompletionDTO dto) {
        String tempToken = dto.getTempToken();

        if (!redisTemplate.hasKey("google_oauth:" + tempToken)) {
            return ResponseEntity.badRequest().body(Map.of("message", "註冊已過期，請重新嘗試"));
        }

        Map<Object, Object> googleData = redisTemplate.opsForHash().entries("google_oauth:" + tempToken);


        UsersDTO usersDTO = new UsersDTO();
        usersDTO.setEmail((String) googleData.get("email"));
        usersDTO.setName((String) googleData.get("name"));
        usersDTO.setTwId(dto.getTwId());
        usersDTO.setPhone(dto.getPhone());

        usersService.registerUser(usersDTO);

        redisTemplate.delete("google_oauth:" + tempToken);


        Map<String, Object> loginResult = usersService.loginWithOauth(usersDTO.getEmail());
        return ResponseEntity.ok(loginResult);
    }
}
