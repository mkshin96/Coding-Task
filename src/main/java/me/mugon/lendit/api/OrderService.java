package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.AccountRepository;
import me.mugon.lendit.domain.order.Orders;
import me.mugon.lendit.domain.order.OrdersRepository;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.domain.product.ProductRepository;
import me.mugon.lendit.web.dto.account.AccountRequestDto;
import me.mugon.lendit.web.dto.order.OrdersRequestDto;
import me.mugon.lendit.web.dto.order.OrdersResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrdersRepository ordersRepository;

    private final ProductService productService;

    @Transactional
    public ResponseEntity<?> order(OrdersRequestDto requestDto, Account currentUser, Long productId) {
        if (requestDto.verifyBalance(currentUser)) { //상품 총액이 예치금보다 많을 경우
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Product> currentProduct = productService.findById(productId);
        if (!currentProduct.isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Product product = currentProduct.get();
        if (requestDto.verifyAmount(product)) { //주문하려는 상품 개수가 재고보다 많을 경우
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Orders orders = requestDto.toEntity(currentUser, product);
        Orders savedOrders = ordersRepository.save(orders);
        
        product.reduceAmount(requestDto.getNumber());
        currentUser.reduceBalance(requestDto.getTotal());

        return new ResponseEntity<>(new OrdersResponseDto(savedOrders), HttpStatus.CREATED);
    }
}
