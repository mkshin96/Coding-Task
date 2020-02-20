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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static me.mugon.lendit.api.error.ErrorMessageConstant.KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private final String BEARER = "Bearer ";
    private final String username = "사용자";
    private final String password = "password";
    private final String anotherUsername = "다른 사용자";

    @AfterEach
    void clean() {
        ordersRepository.deleteAll();
        productRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("주문이 정상적으로 등록되는지 테스트")
    void 주문_리스트_등록_테스트() throws Exception {
        Account account = generateAccount(username, password, 1000L);
        Account anotherAccount = generateAccount(anotherUsername, password, 500000000000L);

        Account saveAccount = saveAccount(account);
        Account saveAnotherAccount = saveAccount(anotherAccount);

        List<OrdersRequestDto> list = new LinkedList<>();

        IntStream.rangeClosed(1, 10).forEach(i -> {
            Product product = generateProduct(1000L, 10L, saveAccount);
            Product saveProduct = saveProduct(product);
            OrdersRequestDto dto = OrdersRequestDto.builder()
                    .number(3L)
                    .total(3000L)
                    .product(saveProduct)
                    .build();
            list.add(dto);
        });

        assertEquals(list.size(), 10);

        mockMvc.perform(post(ordersUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwtProvider.generateToken(saveAnotherAccount))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(list)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("[*].id").exists())
                .andExpect(jsonPath("[*].total").exists())
                .andExpect(jsonPath("[*].number").exists())
                .andExpect(jsonPath("[*].createdAt").exists());
    }

    @Test
    @DisplayName("자신이 등록한 상품을 자신이 주문하려고 할 경우 Bad Request 반환")
    void 자신이_등록한_상품_주문() throws Exception {
        long balance = 5000000L;
        long price = 10000L;
        long number = 15L;
        long amount = 30L;

        Account account = generateAccount(username, password, balance);
        Account savedAccount = saveAccount(account);

        Product product = generateProduct(price, amount, savedAccount);
        Product savedProduct = saveProduct(product);

        OrdersRequestDto ordersRequestDto = OrdersRequestDto.builder()
                .number(number)
                .total(number * price)
                .product(savedProduct)
                .build();

        List<OrdersRequestDto> ordersRequestDtos = Arrays.asList(ordersRequestDto);

        mockMvc.perform(post(ordersUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwtProvider.generateToken(savedAccount))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtos)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @Test
    @DisplayName("주문하려는 유저의 예치금보다 상품이 비쌀 경우 Bad Request 반환")
    void 예치금_보다_상품이_비쌀경우() throws Exception {
        long balance = 10000L;
        long price = 11000L;
        long number = 5L;
        long amount = 30L;

        Account account = generateAccount(username, password, balance);
        Account savedAccount = saveAccount(account);

        Product product = generateProduct(price, amount, savedAccount);
        Product savedProduct = saveProduct(product);

        OrdersRequestDto ordersRequestDto = OrdersRequestDto.builder()
                .number(number)
                .total(number * price)
                .product(savedProduct)
                .build();

        List<OrdersRequestDto> ordersRequestDtos = Arrays.asList(ordersRequestDto);

        mockMvc.perform(post(ordersUrl)
                        .header(HttpHeaders.AUTHORIZATION, BEARER + jwtProvider.generateToken(savedAccount))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ordersRequestDtos)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());

        Optional<Account> byUsername = accountRepository.findByUsername(savedAccount.getUsername());
        Optional<Product> findById = productRepository.findById(savedProduct.getId());
        assertEquals(byUsername.get().getBalance(), balance);
        assertEquals(findById.get().getAmount(), amount);
    }

    @Test
    @DisplayName("주문하려는 상품의 수가 상품의 재고보다 많을 경우 Bad Request 반환")
    void 상품의_수가_재고보다_많을_경우() throws Exception {
        long balance = 5000000L;
        long price = 11000L;
        long amount = 30L;
        long number = 40L;
        Account account = generateAccount(username, password, balance);
        Account anotherAccount = generateAccount(anotherUsername, password, balance);
        Account savedAccount = saveAccount(account);
        Account anotherSavedAccount = saveAccount(anotherAccount);

        Product product = generateProduct(price, amount, savedAccount);
        Product savedProduct = saveProduct(product);

        OrdersRequestDto ordersRequestDto = OrdersRequestDto.builder()
                .number(number)
                .total(number * price)
                .product(savedProduct)
                .build();

        List<OrdersRequestDto> ordersRequestDtos = Arrays.asList(ordersRequestDto);

        mockMvc.perform(post(ordersUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwtProvider.generateToken(anotherSavedAccount))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtos)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());

        Optional<Product> findById = productRepository.findById(savedProduct.getId());
        assertEquals(findById.get().getAmount(), amount);
    }

    @Test
    @DisplayName("상품의 재고가 0이 된 경우 상품을 더 이상 주문할 수 없는지 테스트")
    void 상품_재고_0_삭제() throws Exception {
        long balance = 300000L;
        long price = 10000L;
        long number = 30L;
        long amount = 30L;

        Account account = generateAccount(username, password, balance);
        Account anotherAccount = generateAccount(anotherUsername, password, balance);
        Account savedAccount = saveAccount(account);
        Account anotherSavedAccount = saveAccount(anotherAccount);

        Product product = generateProduct(price, amount, savedAccount);
        Product savedProduct = saveProduct(product);

        OrdersRequestDto ordersRequestDto = OrdersRequestDto.builder()
                .number(number)
                .total(number * price)
                .product(savedProduct)
                .build();

        List<OrdersRequestDto> ordersRequestDtos = Arrays.asList(ordersRequestDto);

        mockMvc.perform(post(ordersUrl)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + jwtProvider.generateToken(anotherSavedAccount))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ordersRequestDtos)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("[*].id").exists())
                .andExpect(jsonPath("[*].total").exists())
                .andExpect(jsonPath("[*].number").exists())
                .andExpect(jsonPath("[*].createdAt").exists());

        mockMvc.perform(post(ordersUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER + jwtProvider.generateToken(anotherSavedAccount))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordersRequestDtos)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    private Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    private Product generateProduct(long price, long amount, Account account) {
        return Product.builder()
                    .name("열혈 자바 프로그래밍")
                    .price(price)
                    .amount(amount)
                    .account(account)
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