package me.mugon.lendit.domain.order;

import me.mugon.lendit.domain.BaseValidator;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.web.dto.order.OrdersRequestDto;
import org.springframework.stereotype.Component;

@Component
public class OrdersValidator extends BaseValidator {

    public boolean verifyBalance(OrdersRequestDto requestDto, Account account) {
        return requestDto.verifyBalance(account);
    }

    public boolean verifyAmount(OrdersRequestDto requestDto, Product product) {
        return requestDto.verifyAmount(product);
    }
}
