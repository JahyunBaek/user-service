package com.example.userservice.User.client;

import com.example.userservice.User.vo.ResponseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "orders-service")
public interface OrderServiceClient {
    @GetMapping(value="/orders-service/{userId}/orders")
    public List<ResponseOrder> getOrders(@PathVariable("userId") String userId);
}
