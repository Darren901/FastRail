package com.example.fastrail.repository;

import com.example.fastrail.model.Orders;
import com.example.fastrail.model.Trains;
import com.example.fastrail.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    Optional<Orders> findByOrderNumber(String orderNumber);

    List<Orders> findByUser(Users user);

    List<Orders> findByTrain(Trains train);

    boolean existsByOrderNumber(String orderNumber);

    List<Orders> findByOrderStatusAndPaymentDeadlineBefore(Orders.OrderStatus orderStatus, LocalDateTime deadline);

}
