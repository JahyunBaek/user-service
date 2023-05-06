package com.example.userservice.User.client;

import com.example.userservice.User.vo.ResponseOrder;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

@FeignClient(name = "orders-service")
public interface OrderServiceClient {
    @GetMapping(value="/orders-service/{userId}/orders")
    @CircuitBreaker(name = "orders-service", fallbackMethod = "fallback")
    @Retry(name = "orders-service")
    public List<ResponseOrder> getOrders(@PathVariable("userId") String userId);

    default List<ResponseOrder> fallback(Exception ex) {
        return Collections.emptyList();
    }
}
