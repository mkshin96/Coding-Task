package me.mugon.lendit.web;

import me.mugon.lendit.common.BaseControllerTest;
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
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest extends BaseControllerTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    private final String productUrl = "/api/products";

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
                    .header(HttpHeaders.AUTHORIZATION, generateJwt(account))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name", is(name)))
                .andExpect(jsonPath("price", is(15000)))
                .andExpect(jsonPath("amount", is(30)))
                .andExpect(jsonPath("createdAt").exists())
                .andExpect(jsonPath("account.username", is(account.getUsername())))
                .andExpect(jsonPath("account.password").doesNotExist())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-products").exists())
                .andExpect(jsonPath("_links.update-product").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.order").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("create-product",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-products").description("link to query products"),
                                linkWithRel("update-product").description("link to update product"),
                                linkWithRel("delete-product").description("link to delete product"),
                                linkWithRel("order").description("link to order"),
                                linkWithRel("profile").description("link to profile")
                        ), requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Authorization Header")
                        ), requestFields(
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("price").description("상품 가격"),
                                fieldWithPath("amount").description("상품 재고 수량")
                        ), responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), responseFields(
                                fieldWithPath("id").description("상품 식별자"),
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("price").description("상품 가격"),
                                fieldWithPath("amount").description("상품 재고 수량"),
                                fieldWithPath("createdAt").description("상품 등록 일시"),
                                fieldWithPath("account.id").description("상품 등록자 식별자"),
                                fieldWithPath("account.username").description("상품 등록자 이름"),
                                fieldWithPath("account.balance").description("상품 등록자 예치금"),
                                fieldWithPath("account.role").description("상품 등록자 역할"),
                                fieldWithPath("account.createdAt").description("상품 등록자 생성 일시"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ));

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
                .header(HttpHeaders.AUTHORIZATION, generateJwt(account))
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
                    .header(HttpHeaders.AUTHORIZATION,generateJwt(account))
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
        Product savedProduct = saveProduct(price, amount, account);

        ProductRequestDto updateProduct = ProductRequestDto.builder()
                .name(updatedName)
                .price(price)
                .amount(amount)
                .build();

        mockMvc.perform(put(productUrl + "/{productId}", savedProduct.getId())
                .header(HttpHeaders.AUTHORIZATION,generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProduct)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name", is(updatedName)))
                .andExpect(jsonPath("price", is(15000)))
                .andExpect(jsonPath("amount", is(30)))
                .andExpect(jsonPath("createdAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.delete-product").exists())
                .andExpect(jsonPath("_links.query-products").exists())
                .andExpect(jsonPath("_links.create-product").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-product",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-products").description("link to query products"),
                                linkWithRel("delete-product").description("link to delete product"),
                                linkWithRel("create-product").description("link to create product"),
                                linkWithRel("profile").description("link to profile")
                        ), requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Authorization Header")
                        ), requestFields(
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("price").description("상품 가격"),
                                fieldWithPath("amount").description("상품 재고 수량")
                        ), responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), responseFields(
                                fieldWithPath("id").description("상품 식별자"),
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("price").description("상품 가격"),
                                fieldWithPath("amount").description("상품 재고 수량"),
                                fieldWithPath("createdAt").description("상품 등록 일시"),
                                fieldWithPath("account.id").description("상품 등록자 식별자"),
                                fieldWithPath("account.username").description("상품 등록자 이름"),
                                fieldWithPath("account.balance").description("상품 등록자 예치금"),
                                fieldWithPath("account.role").description("상품 등록자 역할"),
                                fieldWithPath("account.createdAt").description("상품 등록자 생성 일시"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ));

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
        Product savedProduct = saveProduct(price, amount, account);

        ProductRequestDto updateProduct = ProductRequestDto.builder()
                .name(emptyName)
                .price(price)
                .amount(amount)
                .build();

        mockMvc.perform(put(productUrl + "/{productId}", savedProduct.getId())
                .header(HttpHeaders.AUTHORIZATION,generateJwt(account))
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
        Product product = saveProduct(price, amount, account);

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
                .header(HttpHeaders.AUTHORIZATION,generateJwt(account))
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
        Product product = saveProduct(price, amount, account);

        ProductRequestDto updateProduct = ProductRequestDto.builder()
                .name(product.getName())
                .price(price)
                .amount(amount)
                .build();

        mockMvc.perform(put(productUrl + "/{productId}", -1)
                .header(HttpHeaders.AUTHORIZATION, generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProduct)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @Test
    @DisplayName("상품 수정 시 상품을 등록한 사용자가 아닐 경우 Bad Request 반환")
    void 상품_수정_등록한_사용자가_아닐_경우() throws Exception {
        long price = 100000L;
        long amount = 10000L;
        Account account = createAccount();
        Account anotherAccount = createAccount_need_account(generateAccount_need_username("anotherUsername"));

        Product product = saveProduct(10000L, 1000L, account);
        ProductRequestDto updateProduct = ProductRequestDto.builder()
                .name(product.getName())
                .price(price)
                .amount(amount)
                .build();

        mockMvc.perform(put(productUrl + "/{productId}", product.getId())
                            .header(HttpHeaders.AUTHORIZATION, generateJwt(anotherAccount))
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
        Product savedProduct = saveProduct(price, amount, account);

        mockMvc.perform(delete(productUrl + "/{productId}", savedProduct.getId())
                .header(HttpHeaders.AUTHORIZATION, generateJwt(account)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.create-product").exists())
                .andExpect(jsonPath("_links.query-products").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("delete-product",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-products").description("link to query products"),
                                linkWithRel("create-product").description("link to create product"),
                                linkWithRel("profile").description("link to profile")
                        ), responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), responseFields(
                                fieldWithPath("id").description("상품 식별자"),
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("price").description("상품 가격"),
                                fieldWithPath("amount").description("상품 재고 수량"),
                                fieldWithPath("createdAt").description("상품 등록 일시"),
                                fieldWithPath("account.id").description("상품 등록자 식별자"),
                                fieldWithPath("account.username").description("상품 등록자 이름"),
                                fieldWithPath("account.balance").description("상품 등록자 예치금"),
                                fieldWithPath("account.role").description("상품 등록자 역할"),
                                fieldWithPath("account.createdAt").description("상품 등록자 생성 일시"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ));

        List<Product> all = productRepository.findAll();
        assertEquals(all.size(), 0);
    }

    @Test
    @DisplayName("상품 삭제 시 삭제하려는 상품이 데이터베이스에 저장되어있지 않은 경우 Bad Request 반환")
    void 상품_삭제_저장안돼있을경우_테스트() throws Exception {
        long price = 15000L;
        long amount = 30L;
        Account account = createAccount();
        saveProduct(price, amount, account);

        mockMvc.perform(delete(productUrl + "/{productId}", -1)
                .header(HttpHeaders.AUTHORIZATION,generateJwt(account)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @Test
    @DisplayName("상품 삭제 시 요청을 한 사용자가 상품을 등록한 사용자가 아닌 경우 Bad Request 반환")
    void 상품_삭제_사용자가_상품을_등록한_사용자가_아닌경우() throws Exception {
        long price = 100000L;
        long amount = 10000L;
        Account account = createAccount();
        Account anotherAccount = createAccount_need_account(generateAccount_need_username("anotherUsername"));

        Product product = saveProduct(price, amount, account);

        mockMvc.perform(delete(productUrl + "/{productId}", product.getId())
                .header(HttpHeaders.AUTHORIZATION, generateJwt(anotherAccount)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @DisplayName("상품 리스트 조회 테스트")
    @Test
    void 상품_리스트_조회_테스트() throws Exception {
        long price = 15000L;
        long amount = 30L;
        Account account = createAccount();
        IntStream.rangeClosed(1, 30).forEach(e -> saveProduct_need_index(e, price, amount, account));

        List<Product> all = productRepository.findAll();
        assertEquals(all.size(), 30);

        mockMvc.perform(get(productUrl))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].id").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].name").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].price").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].amount").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].createdAt").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].account.id").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].account.username").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].account.balance").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].account.role").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*].account.createdAt").exists())
                .andExpect(jsonPath("_embedded.productResponseDtoList[*]._links.self.href").exists())
                .andExpect(jsonPath("_links.first").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-products",
                        links(
                                linkWithRel("first").description("link to first page"),
                                linkWithRel("self").description("link to self"),
                                linkWithRel("next").description("link to next page"),
                                linkWithRel("last").description("link to last page"),
                                linkWithRel("profile").description("link to profile")
                        ), responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), responseFields(
                                fieldWithPath("_embedded.productResponseDtoList[*].id").description("상품 식별자"),
                                fieldWithPath("_embedded.productResponseDtoList[*].name").description("상품 이름"),
                                fieldWithPath("_embedded.productResponseDtoList[*].price").description("상품 가격"),
                                fieldWithPath("_embedded.productResponseDtoList[*].amount").description("상품 재고 수량"),
                                fieldWithPath("_embedded.productResponseDtoList[*].createdAt").description("상품 등록 일시"),
                                fieldWithPath("_embedded.productResponseDtoList[*].account").description("상품 등록자 식별자"),
                                fieldWithPath("_embedded.productResponseDtoList[*].account.id").description("상품 등록자 식별자"),
                                fieldWithPath("_embedded.productResponseDtoList[*].account.username").description("상품 등록자 이름"),
                                fieldWithPath("_embedded.productResponseDtoList[*].account.balance").description("상품 등록자 예치금"),
                                fieldWithPath("_embedded.productResponseDtoList[*].account.role").description("상품 등록자 역할"),
                                fieldWithPath("_embedded.productResponseDtoList[*].account.createdAt").description("상품 등록자 생성 일시"),
                                fieldWithPath("_embedded.productResponseDtoList[*]._links.self.href").description("link to self"),
                                fieldWithPath("_links.*.*").ignored(),
                                fieldWithPath("page.size").description("한 페이지의 항목 개수"),
                                fieldWithPath("page.totalElements").description("총 항목 개수"),
                                fieldWithPath("page.totalPages").description("총 페이지 개수"),
                                fieldWithPath("page.number").description("현재 페이지 번호(0부터 시작)")
                        )
                ));
    }

    @Test
    @DisplayName("개별 상품 조회 테스트")
    void 개별_상품_조회() throws Exception {
        Account account = createAccount();
        IntStream.rangeClosed(1, 10).forEach(index -> {
            long price = 15000L;
            long amount = 30L;
            saveProduct_need_index(index, price, amount, account);
        });

        mockMvc.perform(get(productUrl + "/{productId}", 3))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("price", is(15000)))
                .andExpect(jsonPath("amount", is(30)))
                .andExpect(jsonPath("createdAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.order").exists())
                .andExpect(jsonPath("_links.query-products").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-product",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("order").description("link to order"),
                                linkWithRel("query-products").description("link to query products"),
                                linkWithRel("profile").description("link to profile")
                        ), responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), responseFields(
                                fieldWithPath("id").description("상품 식별자"),
                                fieldWithPath("name").description("상품 이름"),
                                fieldWithPath("price").description("상품 가격"),
                                fieldWithPath("amount").description("상품 재고 수량"),
                                fieldWithPath("createdAt").description("상품 등록 일시"),
                                fieldWithPath("account.id").description("상품 등록자 식별자"),
                                fieldWithPath("account.username").description("상품 등록자 이름"),
                                fieldWithPath("account.balance").description("상품 등록자 예치금"),
                                fieldWithPath("account.role").description("상품 등록자 역할"),
                                fieldWithPath("account.createdAt").description("상품 등록자 생성 일시"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ));

        List<Product> all = productRepository.findAll();
        assertEquals(all.size(), 10);
    }

    private Product saveProduct(long price, long amount, Account account) {
        return productRepository.save(Product.builder()
                .name("스타트 스프링 부트")
                .price(price)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .account(account)
                .build());
    }

    private Product saveProduct_need_index(int index, long price, long amount, Account account) {
        return productRepository.save(Product.builder()
                .name("스타트 스프링 부트" + index)
                .price(price)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .account(account)
                .build());
    }

    private Account createAccount() {
        return accountRepository.save(generateAccount());
    }

    private Account createAccount_need_account(Account account) {
        return accountRepository.save(account);
    }

    private Account generateAccount() {
        return Account.builder()
                .username("username")
                .password("password")
                .createdAt(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .build();
    }

    private Account generateAccount_need_username(String username) {
        return Account.builder()
                .username(username)
                .password("password")
                .createdAt(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .build();
    }
}