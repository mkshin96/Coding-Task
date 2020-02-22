package me.mugon.lendit.web;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.OrderService;
import me.mugon.lendit.domain.BaseValidator;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.CurrentUser;
import me.mugon.lendit.web.dto.order.OrdersRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/orders")
@RestController
public class OrdersController {

    private final OrderService orderService;

    private final BaseValidator baseValidator;

    @PostMapping
    public ResponseEntity<?> order(@Valid  @RequestBody OrdersRequestDto[] requestDto, @CurrentUser Account currentUser, Errors errors) {
        if (errors.hasErrors()) {
            return baseValidator.returnErrors(errors);
        }
        return orderService.order(requestDto, currentUser);
    }
}
