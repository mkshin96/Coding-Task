package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.domain.order.OrdersRepository;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.web.dto.order.OrdersRequestDto;
import me.mugon.lendit.web.dto.order.OrdersResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static me.mugon.lendit.api.error.ErrorMessageConstant.*;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrdersRepository ordersRepository;

    private final ProductService productService;

    private final AccountService accountService;

    @Transactional
    public ResponseEntity<?> order(OrdersRequestDto[] requestDto, Account currentUser) {
        List<Orders> ordersList = new LinkedList<>();
        for (OrdersRequestDto order : requestDto) {
            if (order.getProduct().isCheckAmount()) {
                return new ResponseEntity<>(SHORTAGEOFGOODS, HttpStatus.BAD_REQUEST);
            }
            Long id = order.getProduct().getId();
            Optional<Product> optionalProduct = productService.findById(id);
            if (!optionalProduct.isPresent()) {
                return new ResponseEntity<>(getErrorMap(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
            }
            Product product = optionalProduct.get();
            if (verifyAmount(order, order.getProduct())) {
                return new ResponseEntity<>(getErrorMap(SHORTAGEOFGOODS), HttpStatus.BAD_REQUEST);
            }
            Optional<Account> optionalAccount = accountService.findById(currentUser.getId());
            if (!optionalAccount.isPresent()) {
                return new ResponseEntity<>(getErrorMap(USERNOTFOUND), HttpStatus.BAD_REQUEST);
            }
            Account account = optionalAccount.get();
            if (verifyBalance(order, currentUser)) {
                return new ResponseEntity<>(getErrorMap(OVERTHELIMIT), HttpStatus.BAD_REQUEST);
            }
            if (currentUser.getId().equals(order.getProduct().getAccount().getId())) {
                return new ResponseEntity<>(getErrorMap(REGISTEREDBYONESELF), HttpStatus.BAD_REQUEST);
            }
            account.reduceBalance(order.getTotal());
            product.reduceAmount(order.getNumber());
            if (product.getAmount() == 0) {
                product.amountIsZero();
            }
            Orders orders = order.toEntity(currentUser);
            ordersList.add(orders);
        }
        ordersRepository.saveAll(ordersList);
        List<OrdersResponseDto> collect = ordersList.stream()
                .map(OrdersResponseDto::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(collect ,HttpStatus.CREATED);
    }

    private boolean verifyBalance(OrdersRequestDto requestDto, Account account) {
        return requestDto.verifyBalance(account);
    }

    private boolean verifyAmount(OrdersRequestDto requestDto, Product product) {
        return requestDto.verifyAmount(product);
    }

    private Map<String, List<String>> getErrorMap(String message) {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(KEY, Arrays.asList(message));
        return errors;
    }
}
