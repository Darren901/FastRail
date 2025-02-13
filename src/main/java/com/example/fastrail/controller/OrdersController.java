package com.example.fastrail.controller;

import com.example.fastrail.dto.OrdersDTO;
import com.example.fastrail.model.Orders;
import com.example.fastrail.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/orders")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

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
        Orders order = ordersService.createOrder(ordersDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PutMapping("/payOrder/{orderNumber}")
    public ResponseEntity<?> payOrder(@PathVariable String orderNumber){
        Orders order = ordersService.payOrder(orderNumber);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }
}
