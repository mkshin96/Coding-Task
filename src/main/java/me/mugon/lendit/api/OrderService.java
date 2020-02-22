package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.domain.order.OrdersValidator;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.domain.order.OrdersRepository;
import me.mugon.lendit.domain.order.OrdersResource;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.web.OrdersController;
import me.mugon.lendit.web.ProductController;
import me.mugon.lendit.web.dto.order.OrdersRequestDto;
import me.mugon.lendit.web.dto.order.OrdersResponseDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static me.mugon.lendit.api.error.ErrorMessageConstant.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrdersRepository ordersRepository;

    private final ProductService productService;

    private final AccountService accountService;

    private final OrdersValidator ordersValidator;

    @Transactional
    public ResponseEntity<?> order(OrdersRequestDto[] requestDto, Account currentUser) {
        List<Orders> ordersList = new LinkedList<>();
        for (OrdersRequestDto order : requestDto) {
            if (order.getProduct().getAmount() == 0) {
                order.getProduct().amountIsZero();
            }
            if (order.getProduct().isCheckAmount()) {
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(SHORTAGEOFGOODS), HttpStatus.BAD_REQUEST);
            }
            Long id = order.getProduct().getId();
            Optional<Product> optionalProduct = productService.findById(id);
            if (!optionalProduct.isPresent()) {
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
            }
            Product product = optionalProduct.get();
            if (ordersValidator.verifyAmount(order, order.getProduct())) {
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(SHORTAGEOFGOODS), HttpStatus.BAD_REQUEST);
            }
            Optional<Account> optionalAccount = accountService.findById(currentUser.getId());
            if (!optionalAccount.isPresent()) {
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(USERNOTFOUND), HttpStatus.BAD_REQUEST);
            }
            Account account = optionalAccount.get();
            if (ordersValidator.verifyBalance(order, currentUser)) {
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(OVERTHELIMIT), HttpStatus.BAD_REQUEST);
            }
            if (currentUser.getId().equals(order.getProduct().getAccount().getId())) {
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(REGISTEREDBYONESELF), HttpStatus.BAD_REQUEST);
            }
            account.reduceBalance(order.getTotal());
            product.reduceAmount(order.getNumber());
            if (product.getAmount() == 0) {
                product.amountIsZero();
            }
            Orders orders = order.toEntity(currentUser);
            ordersList.add(orders);
        }
        List<Orders> savedOrdersList = ordersRepository.saveAll(ordersList);

        List<EntityModel<OrdersResponseDto>> collect = savedOrdersList.stream()
                .map(e -> {
                    EntityModel<OrdersResponseDto> entityModel = new EntityModel<>(new OrdersResponseDto(e));
                    entityModel.add(linkTo(ProductController.class).slash(e.getId()).withSelfRel());
                    return entityModel;
                }).collect(Collectors.toList());

        OrdersResource entityModels = new OrdersResource(collect);
        entityModels.add(linkTo(OrdersController.class).withSelfRel());
        entityModels.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-orders-create").withRel("profile"));
        entityModels.add(linkTo(ProductController.class).withRel("create-product"));
        entityModels.add(linkTo(ProductController.class).withRel("query-products"));
        return new ResponseEntity<>(entityModels, HttpStatus.CREATED);
    }
}
