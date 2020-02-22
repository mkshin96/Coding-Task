package me.mugon.lendit.web;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.OrderService;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.CurrentUser;
import me.mugon.lendit.domain.order.OrdersValidator;
import me.mugon.lendit.web.dto.order.OrdersRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/orders")
@RestController
public class OrdersController {

    private final OrderService orderService;

    private final OrdersValidator ordersValidator;

    @PostMapping
    public ResponseEntity<?> order(@RequestBody List<@Valid OrdersRequestDto> requestDto, @CurrentUser Account currentUser, Errors errors) throws BindException {
        ordersValidator.validate(requestDto, errors);
        if (errors.hasErrors()) {
            return ordersValidator.returnErrors(errors);
        }
        return orderService.order(requestDto, currentUser);
    }
}
