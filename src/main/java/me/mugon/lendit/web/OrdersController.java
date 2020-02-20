package me.mugon.lendit.web;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.OrderService;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.CurrentUser;
import me.mugon.lendit.web.dto.order.OrdersRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/orders")
@RestController
public class OrdersController {

    private final OrderService orderService;

    @PostMapping("/{productId}")
    public ResponseEntity<?> order(@PathVariable Long productId, @RequestBody OrdersRequestDto requestDto, @CurrentUser Account currentUser) {
        return orderService.order(requestDto, currentUser, productId);
    }
}
