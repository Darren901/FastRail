package com.example.fastrail.controller;

import com.example.fastrail.config.RabbitMQConfig;
import com.example.fastrail.dto.OrdersDTO;
import com.example.fastrail.model.Orders;
import com.example.fastrail.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<?> findByOrderNumber(@PathVariable String orderNumber){
        Orders order = ordersService.findByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> findByUserId(@PathVariable Integer userId){
        List<Orders> orders = ordersService.findByUser(userId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrdersDTO ordersDTO){
        if (ordersDTO.getClientOrderId() == null || ordersDTO.getClientOrderId().isBlank()) {
            ordersDTO.setClientOrderId(UUID.randomUUID().toString());
        }

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                ordersDTO
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "訂單已排隊，稍後通知結果");
        response.put("clientOrderId", ordersDTO.getClientOrderId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PutMapping("/payOrder/{orderNumber}")
    public ResponseEntity<?> payOrder(@PathVariable String orderNumber, @RequestBody Map<String, String> payLoad){
        Orders order = ordersService.payOrder(orderNumber, payLoad.get("twId"));
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    @GetMapping("/status/{clientOrderId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable String clientOrderId) {
        String key = "clientOrder:" + clientOrderId;
        String resultJson = redisTemplate.opsForValue().get(key);
        if (resultJson == null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("處理中");
        }
        return ResponseEntity.ok(resultJson);
    }
}
