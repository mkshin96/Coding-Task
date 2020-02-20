package me.mugon.lendit.web;

import me.mugon.lendit.common.BaseControllerTest;
import me.mugon.lendit.config.jwt.JwtProvider;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.AccountRepository;
import me.mugon.lendit.domain.account.Role;
import me.mugon.lendit.domain.order.OrdersRepository;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.domain.product.ProductRepository;
import me.mugon.lendit.web.dto.order.OrdersRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrdersControllerTest extends BaseControllerTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private final String ordersUrl = "/api/orders";

    @Test
    @DisplayName("정상적으로 주문이 완료되었을 때 테스트")
    void 주문_테스트() throws Exception {
        String username = "사용자";
        String password = "password";
        long balance = 5000000L;
        long price = 10000L;
        long amount = 30L;
        long number = 15L;

        Account account = generateAccount(username, password, balance);
        Account savedAccount = saveAccount(account);

        Product product = generateProduct(price, amount);
        Product savedProduct = saveProduct(product);

        OrdersRequestDto ordersRequestDto = OrdersRequestDto.builder()
                .number(number)
                .total(number * price)
                .createdAt(LocalDateTime.now())
                .build();

        mockMvc.perform(post(ordersUrl + "/{productId}", savedProduct.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtProvider.generateToken(savedAccount))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ordersRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("account").exists())
                .andExpect(jsonPath("product").exists())
                .andExpect(jsonPath("number", is(15)))
                .andExpect(jsonPath("total", is(15 * 10000)));
    }


    private Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    private Product generateProduct(long price, long amount) {
        return Product.builder()
                    .name("열혈 자바 프로그래밍")
                    .price(price)
                    .amount(amount)
                    .createdAt(LocalDateTime.now()).build();
    }

    private Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    private Account generateAccount(String username, String password, long balance) {
        return Account.builder()
                    .username(username)
                    .password(password)
                    .balance(balance)
                    .role(Role.ROLE_USER)
                    .createdAt(LocalDateTime.now())
                    .build();
    }
}