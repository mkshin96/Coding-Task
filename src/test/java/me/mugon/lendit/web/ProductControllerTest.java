package me.mugon.lendit.web;

import me.mugon.lendit.common.BaseControllerTest;
import me.mugon.lendit.config.jwt.JwtProvider;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.AccountRepository;
import me.mugon.lendit.domain.account.Role;
import me.mugon.lendit.domain.product.Product;
import me.mugon.lendit.domain.product.ProductRepository;
import me.mugon.lendit.web.dto.product.ProductRequestDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static me.mugon.lendit.api.error.ErrorMessageConstant.KEY;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest extends BaseControllerTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private final String productUrl = "/api/products";

    private final String BEARER = "Bearer ";

    @AfterEach
    void clean() {
        productRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적으로 상품이 등록되는지 테스트")
    void 상품_등록_테스트() throws Exception {
        String name = "스타트 스프링 부트";
        long price = 15000L;
        long amount = 30L;
        Account account = createAccount();
        ProductRequestDto productRequestDto = ProductRequestDto.builder()
                .name(name)
                .price(price)
                .amount(amount)
                .build();

        mockMvc.perform(post(productUrl)
                    .header(HttpHeaders.AUTHORIZATION,BEARER + generateJwt(account))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name", is(name)))
                .andExpect(jsonPath("price", is(15000)))
                .andExpect(jsonPath("amount", is(30)))
                .andExpect(jsonPath("createdAt").exists())
                .andExpect(jsonPath("account.username", is(account.getUsername())));

        List<Product> findAll = productRepository.findAll();
        assertEquals(findAll.get(0).getName(), name);
        assertEquals(findAll.get(0).getPrice(), price);
        assertEquals(findAll.get(0).getAmount(), amount);
    }

    @DisplayName("상품 생성 시 이름이 공백일 경우 Bad Request 반환")
    @ParameterizedTest(name = "{displayName}{index}")
    @ValueSource(strings = {"", "          "})
    void 상품_등록_상품이름_공백_테스트(String emptyName) throws Exception {
        long price = 15000L;
        long amount = 30L;
        Account account = createAccount();

        ProductRequestDto productRequestDto = ProductRequestDto.builder()
                .name(emptyName)
                .price(price)
                .amount(amount)
                .build();

        mockMvc.perform(post(productUrl)
                .header(HttpHeaders.AUTHORIZATION,BEARER + generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());

        List<Product> all = productRepository.findAll();
        assertEquals(all.size(), 0);
    }

    @DisplayName("상품 생성 시 이름, 가격, 수량 중 하나라도 null이 들어올 경우 BadRequest 반환")
    @RepeatedTest(value = 3, name = "{displayName}, {currentRepetition}/{totalRepetitions}")
    void 상품_등록_null_테스트(RepetitionInfo info) throws Exception {
        Account account = createAccount();
        if (info.getCurrentRepetition() == 1) {
            ProductRequestDto productRequestDto = ProductRequestDto.builder()
                    .name("스타트 스프링 부트")
                    .price(15000L)
                    .amount(30l)
                    .build();

            int currentRepetition = info.getCurrentRepetition();
            if (currentRepetition == 1) {
                productRequestDto.setName(null);
            } else if (currentRepetition == 2) {
                productRequestDto.setPrice(null);
            } else if (currentRepetition == 3) {
                productRequestDto.setAmount(null);
            }

            mockMvc.perform(post(productUrl)
                    .header(HttpHeaders.AUTHORIZATION,BEARER + generateJwt(account))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("message").exists());

            List<Product> all = productRepository.findAll();
            assertEquals(all.size(), 0);
        }
    }

    @Test
    @DisplayName("정상적으로 상품이 수정되는지 테스트")
    void 상품_수정_테스트() throws Exception {
        long price = 15000L;
        long amount = 30L;
        String updatedName = "모던 자바 인 액션";
        Account account = createAccount();
        Product savedProduct = saveProduct(price, amount);

        ProductRequestDto updateProduct = ProductRequestDto.builder()
                .name(updatedName)
                .price(price)
                .amount(amount)
                .build();

        mockMvc.perform(put(productUrl + "/{productId}", savedProduct.getId())
                .header(HttpHeaders.AUTHORIZATION,BEARER + generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProduct)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name", is(updatedName)))
                .andExpect(jsonPath("price", is(15000)))
                .andExpect(jsonPath("amount", is(30)))
                .andExpect(jsonPath("createdAt").exists());

        List<Product> findAll = productRepository.findAll();
        assertEquals(findAll.get(0).getName(), updatedName);
        assertEquals(findAll.get(0).getPrice(), price);
        assertEquals(findAll.get(0).getAmount(), amount);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "       "})
    @DisplayName("상품 수정 시 이름이 공백일 경우 Bad Request 반환")
    void 상품수정_상품이름_공백테스트(String emptyName) throws Exception {
        long price = 15000L;
        long amount = 30L;
        Account account = createAccount();
        Product savedProduct = saveProduct(price, amount);

        ProductRequestDto updateProduct = ProductRequestDto.builder()
                .name(emptyName)
                .price(price)
                .amount(amount)
                .build();

        mockMvc.perform(put(productUrl + "/{productId}", savedProduct.getId())
                .header(HttpHeaders.AUTHORIZATION,BEARER + generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProduct)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @DisplayName("상품 수정 시 이름, 가격, 수량 중 하나라도 null일 경우 Bad Request 반환")
    @RepeatedTest(value = 3, name = "{displayName}, {currentRepetition}/{totalRepetitions}")
    void 상품_수정_null_테스트(RepetitionInfo info) throws Exception {
        String name = "스타트 스프링 부트";
        long price = 15000L;
        long amount = 30L;
        Account account = createAccount();
        Product product = saveProduct(price, amount);

        ProductRequestDto updateProduct = ProductRequestDto.builder()
                .name(name)
                .price(price)
                .amount(amount)
                .build();

        int currentRepetition = info.getCurrentRepetition();
        if (currentRepetition == 1) {
            updateProduct.setName(null);
        } else if (currentRepetition == 2) {
            updateProduct.setPrice(null);
        } else if (currentRepetition == 3) {
            updateProduct.setAmount(null);
        }

        mockMvc.perform(put(productUrl + "/{productUrl}", product.getId())
                .header(HttpHeaders.AUTHORIZATION,BEARER + generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProduct)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @DisplayName("상품 수정 시 수정하려는 상품이 데이터베이스에 저장되어 있지 않을 경우 Bad Request 반환")
    @Test
    void 상품_수정_저장안돼있을경우_테스트() throws Exception {
        long price = 15000L;
        long amount = 30L;
        Account account = createAccount();
        Product product = saveProduct(price, amount);

        ProductRequestDto updateProduct = ProductRequestDto.builder()
                .name(product.getName())
                .price(price)
                .amount(amount)
                .build();

        mockMvc.perform(put(productUrl + "/{productId}", -1)
                .header(HttpHeaders.AUTHORIZATION,BEARER + generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProduct)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @Test
    @DisplayName("정상적으로 상품이 삭제되는지 테스트")
    void 상품_삭제_테스트() throws Exception{
        long price = 15000L;
        long amount = 30L;
        Account account = createAccount();
        Product savedProduct = saveProduct(price, amount);

        mockMvc.perform(delete(productUrl + "/{productId}", savedProduct.getId())
                .header(HttpHeaders.AUTHORIZATION,BEARER + generateJwt(account)))
                .andDo(print())
                .andExpect(status().isOk());

        List<Product> all = productRepository.findAll();
        assertEquals(all.size(), 0);
    }

    @Test
    @DisplayName("상품 삭제 시 삭제하려는 상품이 데이터베이스에 저장되어있지 않은 경우 Bad Request 반환")
    void 상품_삭제_저장안돼있을경우_테스트() throws Exception {
        long price = 15000L;
        long amount = 30L;
        Account account = createAccount();
        saveProduct(price, amount);

        mockMvc.perform(delete(productUrl + "/{productId}", -1)
                .header(HttpHeaders.AUTHORIZATION,BEARER + generateJwt(account)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @DisplayName("상품 리스트 조회 테스트")
    @Test
    void 상품_리스트_조회_테스트() throws Exception {
        long price = 15000L;
        long amount = 30L;
        IntStream.rangeClosed(1, 30).forEach(e -> saveProduct(price, amount));

        List<Product> all = productRepository.findAll();
        assertEquals(all.size(), 30);

        mockMvc.perform(get(productUrl))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private Product saveProduct(long price, long amount) {
        return productRepository.save(Product.builder()
                .name("스타트 스프링 부트")
                .price(price)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private String generateJwt(Account account) {
        return jwtProvider.generateToken(account);
    }

    private Account createAccount() {
        return accountRepository.save(generateAccount());
    }

    private Account generateAccount() {
        return Account.builder()
                .username("username")
                .password("password")
                .createdAt(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .build();
    }
}