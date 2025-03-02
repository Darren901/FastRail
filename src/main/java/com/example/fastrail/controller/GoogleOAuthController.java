package com.example.fastrail.controller;

import com.example.fastrail.config.GoogleConfig;
import com.example.fastrail.dto.RegistrationCompletionDTO;
import com.example.fastrail.dto.UsersDTO;
import com.example.fastrail.service.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/google")
public class GoogleOAuthController {

    @Autowired
    private GoogleConfig googleConfig;

    @Autowired
    private UsersService usersService;

    @Autowired
    private StringRedisTemplate redisTemplate;

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

        System.out.println(result);

        JsonNode jsonNode2 = new ObjectMapper().readTree(result);
        String userEmail = jsonNode2.get("email").asText();
        String userName = jsonNode2.get("name").asText();

        if(usersService.existsByEmail(userEmail)){
            Map<String, Object> loginResult = usersService.loginWithOauth(userEmail);
            return ResponseEntity.ok().body(loginResult);

        }else{
            String tempToken = UUID.randomUUID().toString();

            redisTemplate.opsForHash().putAll("google_oauth:" + tempToken, Map.of("email", userEmail, "name", userName));
            redisTemplate.expire("google_oauth:" + tempToken, 10, TimeUnit.MINUTES);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "NEED_MORE_INFO");
            response.put("tempToken", tempToken);
            response.put("email", userEmail);
            response.put("name", userName);

            return ResponseEntity.ok(response);
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
