package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.domain.order.OrdersRepository;
import me.mugon.lendit.domain.order.OrdersResource;
import me.mugon.lendit.domain.order.OrdersValidator;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
    public ResponseEntity<?> order(List<OrdersRequestDto> ordersRequestDtos, Account currentUser) {
        List<Orders> ordersList = new LinkedList<>();

        for (OrdersRequestDto ordersRequestDto : ordersRequestDtos) {
            Long productId = ordersRequestDto.getProductId();
            Optional<Product> optionalProduct = productService.findById(productId);
            if (!optionalProduct.isPresent()) {
                return new ResponseEntity<>(PRODUCTNOTFOUND, HttpStatus.BAD_REQUEST);
            }

            Product savedProduct = optionalProduct.get();
            if (savedProduct.amountEqualsZero()) {
                savedProduct.changeCheckAmount();
            }
            if (savedProduct.isCheckAmount() || ordersRequestDto.verifyAmount(savedProduct)) {
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(SHORTAGEOFGOODS), HttpStatus.BAD_REQUEST);
            }

            Optional<Account> optionalAccount = accountService.findById(currentUser.getId());
            if (!optionalAccount.isPresent()) {
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(USERNOTFOUND), HttpStatus.BAD_REQUEST);
            }
            Account savedAccount = optionalAccount.get();

            if (ordersRequestDto.verifyBalance(savedAccount)) { //예치
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(OVERTHELIMIT), HttpStatus.BAD_REQUEST);
            }
            if (ordersValidator.isValidUser(savedAccount, savedProduct)) {
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(REGISTEREDBYONESELF), HttpStatus.BAD_REQUEST);
            }
            savedAccount.reduceBalance(ordersRequestDto.getTotal());
            savedProduct.reduceAmount(ordersRequestDto.getNumber());

            if (savedProduct.amountEqualsZero()) {
                savedProduct.changeCheckAmount();
            }
            Orders orders = ordersRequestDto.toEntity(currentUser, savedProduct);
            ordersList.add(orders);
        }

        List<Orders> savedOrdersList = ordersRepository.saveAll(ordersList);
        List<EntityModel<OrdersResponseDto>> modelList = savedOrdersList.stream()
                .map(e -> {
                    EntityModel<OrdersResponseDto> entityModel = new EntityModel<>(new OrdersResponseDto(e));
                    entityModel.add(linkTo(ProductController.class).slash(e.getId()).withSelfRel());
                    return entityModel;
                }).collect(Collectors.toList());

        OrdersResource entityModels = new OrdersResource(modelList);
        entityModels.add(linkTo(OrdersController.class).withSelfRel());
        entityModels.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-orders-create").withRel("profile"));
        entityModels.add(linkTo(ProductController.class).withRel("create-product"));
        entityModels.add(linkTo(ProductController.class).withRel("query-products"));
        return new ResponseEntity<>(entityModels, HttpStatus.CREATED);
    }
}
