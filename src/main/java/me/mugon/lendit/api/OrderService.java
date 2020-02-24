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

    /**
     * 1. 입력받은 List를 순회
     * 2. 입력받은 주문의 상품 id로 db를 검색
     * 3. 저장되어 있지 않은 상품인 경우 Body에 'message: 상품을 찾을 수 없습니다.'를 실어 Bad Request와 함께 반환
     * 4. 저장되어 있는 상품인 경우 상품 재고가 0개인지, 주문한 상품의 개수가 재고 수량보다 많은지 검사, 그럴 경우 Body에 'message: 재고가 부족합니다.'를 실어 Bad Request와 함께 반환
     * 5. 현재 로그인한 유저의 id를 통해 db에서 검색
     * 6. db에 등록되어 있지 않은 경우 Body에 'message: 사용자를 찾을 수 없습니다.'를 실어 Bad Request와 함께 반환
     * 7. 유저의 예치금이 상품의 총 가격보다 많은지 검사, 아닐 경우 Body에 'message: 예치금이 부족합니다.'를 실어 Bad Request와 함께 반환
     * 8. 유저가 상품을 등록한 유저인지 검사, 그럴 경우 Body에 'message: 자신이 등록한 상품은 주문할 수 없습니다.'를 실어 Bad Request와 함께 반환
     * 9. 유저의 예치금을 총 가격만큼 감소시킴
     * 10. 상품의 재고 수량을 주문 개수만큼 감소시킴
     * 11. 만약 상품의 재고 수량이 0개인 경우 checkAmount 필드를 true로 변경
     * 12. Orders db에 저장
     * 13. HATEOAS를 위해 create-product, self, query-product 관계를 EntityModel에 더함
     * 14. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 15. 반환
     */
    @Transactional
    public ResponseEntity<?> order(List<OrdersRequestDto> ordersRequestDtos, Account currentUser) {
        List<Orders> ordersList = new LinkedList<>();

        for (OrdersRequestDto ordersRequestDto : ordersRequestDtos) {
            Long productId = ordersRequestDto.getProductId();
            Optional<Product> optionalProduct = productService.findById(productId);
            if (!optionalProduct.isPresent()) { // 상품이 있는지 확인
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(PRODUCTNOTFOUND), HttpStatus.BAD_REQUEST);
            }

            Product savedProduct = optionalProduct.get();
            if (savedProduct.amountEqualsZero() || ordersRequestDto.verifyAmount(savedProduct)) { //상품 재고가 0개가 아닌지, 주문한 상품 개수가 재고 수량보다 적은지 확인
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(SHORTAGEOFGOODS), HttpStatus.BAD_REQUEST);
            }

            Optional<Account> optionalAccount = accountService.findById(currentUser.getId());
            if (!optionalAccount.isPresent()) { // 현재유저가 저장되어 있는 유저인지 확인
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(USERNOTFOUND), HttpStatus.BAD_REQUEST);
            }
            Account savedAccount = optionalAccount.get();

            if (ordersRequestDto.verifyBalance(savedAccount)) { // 현재 유저의 예치금 확인
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(OVERTHELIMIT), HttpStatus.BAD_REQUEST);
            }
            if (ordersValidator.isValidUser(savedAccount, savedProduct)) { //현재 유저가 상품을 등록한 유저가 아닌지 확인
                return new ResponseEntity<>(ordersValidator.returnErrorMessage(REGISTEREDBYONESELF), HttpStatus.BAD_REQUEST);
            }
            savedAccount.reduceBalance(ordersRequestDto.getTotal());
            savedProduct.reduceAmount(ordersRequestDto.getNumber());

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
