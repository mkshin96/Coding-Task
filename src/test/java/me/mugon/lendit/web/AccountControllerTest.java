package me.mugon.lendit.web;

import me.mugon.lendit.common.BaseControllerTest;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.AccountRepository;
import me.mugon.lendit.domain.account.Role;
import me.mugon.lendit.web.dto.account.AccountRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static me.mugon.lendit.api.error.ErrorMessageConstant.KEY;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountControllerTest extends BaseControllerTest {

    @Autowired
    private AccountRepository accountRepository;

    private final String accountUrl = "/api/accounts";

    @AfterEach
    void clean() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적으로 유저가 등록되는지 테스트")
    void 유저_등록_테스트() throws Exception {
        String username = "username";
        String password = "password";
        long balance = 50000000L;

        AccountRequestDto accountRequestDto = AccountRequestDto.builder()
                .username(username)
                .password(password)
                .balance(balance)
                .build();

        mockMvc.perform(post(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("username", is(username)))
                .andExpect(jsonPath("balance", is(50000000)))
                .andExpect(jsonPath("createdAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("create-account",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("login").description("link to login"),
                                linkWithRel("profile").description("link to profile")
                        ), requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), requestFields(
                                fieldWithPath("username").description("사용자가 사용할 이름"),
                                fieldWithPath("password").description("사용자의 비밀번호"),
                                fieldWithPath("balance").description("사용자의 예치금")
                        ), responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), responseFields(
                                fieldWithPath("id").description("식별자"),
                                fieldWithPath("username").description("사용자가 사용할 이름"),
                                fieldWithPath("balance").description("사용자의 예치금"),
                                fieldWithPath("createdAt").description("생성 일시"),
                                fieldWithPath("ordersSet").description("사용자의 주문 리스트"),
                                fieldWithPath("productSet").description("사용자가 등록한 상품 리스트"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ));

        List<Account> findAll = accountRepository.findAll();
        assertEquals(findAll.get(0).getUsername(), username);
        assertEquals(findAll.get(0).getBalance(), balance);
        assertTrue(findAll.get(0).getCreatedAt().isBefore(LocalDateTime.now()));
    }

    @DisplayName("유저 등록 시 유저이름이 공백일 경우 Bad Request 반환")
    @ParameterizedTest(name = "{displayName}{index}")
    @ValueSource(strings = {"", "          "})
    void 유저_등록_유저이름_공백_테스트(String emptyName) throws Exception {
        long balance = 50000000L;
        String password = "password";

        AccountRequestDto accountRequestDto = AccountRequestDto.builder()
                .username(emptyName)
                .password(password)
                .balance(balance)
                .build();

        mockMvc.perform(post(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());

        List<Account> all = accountRepository.findAll();
        assertEquals(all.size(), 0);
    }

    @DisplayName("유저 등록 시 유저이름이 null이 들어올 경우 BadRequest 반환")
    @Test
    void 유저_등록_null_테스트() throws Exception {
        String password = "password";
        AccountRequestDto accountRequestDto = AccountRequestDto.builder()
                .username(null)
                .password(password)
                .balance(500000000L)
                .build();

        mockMvc.perform(post(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());

        List<Account> all = accountRepository.findAll();
            assertEquals(all.size(), 0);
    }

    @DisplayName("유저 등록 시 유저이름이 중복으로 들어올 경우 BadRequest 반환")
    @Test
    void 유저_등록_중복_테스트() throws Exception {
        String username = "username";
        String password = "password";

        AccountRequestDto accountRequestDto = AccountRequestDto.builder()
                .username(username)
                .password(password)
                .balance(500000000L)
                .build();

        mockMvc.perform(post(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath(username, is(username)))
                .andExpect(jsonPath(password).doesNotExist())
                .andExpect(jsonPath("balance", is(500000000)))
                .andExpect(jsonPath("createdAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login").exists());

        mockMvc.perform(post(accountUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @Test
    @DisplayName("정상적으로 유저가 수정되는지 테스트")
    void 유저_수정_테스트() throws Exception {
        Account savedAccount = saveAccount();
        String updatedUsername = "updatedUser";
        long balance = 500000L;

        AccountRequestDto updateAccount = AccountRequestDto.builder()
                .username(updatedUsername)
                .password(savedAccount.getPassword())
                .balance(balance)
                .build();

        mockMvc.perform(put(accountUrl + "/{accountId}", savedAccount.getId())
                .header(HttpHeaders.AUTHORIZATION, generateJwt(savedAccount))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAccount)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("username", is(updatedUsername)))
                .andExpect(jsonPath("balance", is(500000)))
                .andExpect(jsonPath("createdAt").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
                .andExpect(jsonPath("_links.query-products").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-account",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("delete-account").description("link to delete account"),
                                linkWithRel("query-products").description("link to query products"),
                                linkWithRel("profile").description("link to profile")
                        ), requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Authorization Header")
                        ), requestFields(
                                fieldWithPath("username").description("사용자가 사용할 이름"),
                                fieldWithPath("password").description("사용자의 비밀번호"),
                                fieldWithPath("balance").description("사용자의 예치금")
                        ), responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), responseFields(
                                fieldWithPath("id").description("식별자"),
                                fieldWithPath("username").description("사용자가 사용할 이름"),
                                fieldWithPath("balance").description("사용자의 예치금"),
                                fieldWithPath("createdAt").description("생성 일시"),
                                fieldWithPath("ordersSet").description("사용자의 주문 리스트"),
                                fieldWithPath("productSet").description("사용자가 등록한 상품 리스트"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ));

        List<Account> findAll = accountRepository.findAll();
        assertEquals(findAll.get(0).getUsername(), updatedUsername);
        assertEquals(findAll.get(0).getBalance(), balance);
        assertTrue(findAll.get(0).getCreatedAt().isBefore(LocalDateTime.now()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "       "})
    @DisplayName("유저 수정 시 유저이름이 공백일 경우 Bad Request 반환")
    void 유저수정_유저이름_공백테스트(String emptyName) throws Exception {
        Account account = saveAccount();
        long balance = 500000L;

        AccountRequestDto updateAccount = AccountRequestDto.builder()
                .username(emptyName)
                .balance(balance)
                .build();

        mockMvc.perform(put(accountUrl + "/{accountId}", account.getId())
                .header(HttpHeaders.AUTHORIZATION, generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAccount)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @Test
    @DisplayName("유저 수정 시 유저이름이 null이 들어올 경우 BadRequest 반환")
    void 유저_수정_null_테스트() throws Exception {
        Account account = saveAccount();
        long balance = 500000L;

        AccountRequestDto updateAccount = AccountRequestDto.builder()
                .username(null)
                .balance(balance)
                .build();

        mockMvc.perform(put(accountUrl + "/{accountId}", account.getId())
                .header(HttpHeaders.AUTHORIZATION, generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAccount)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @Test
    @DisplayName("유저 수정 시 수정하려는 유저가 데이터베이스에 저장되어 있지 않을 경우 Bad Request 반환")
    void 유저_수정_저장안돼있을경우_테스트() throws Exception {
        Account account = saveAccount();
        long balance = 500000L;

        AccountRequestDto updateAccount = AccountRequestDto.builder()
                .username(null)
                .balance(balance)
                .build();

        mockMvc.perform(put(accountUrl + "/{accountId}", -1)
                .header(HttpHeaders.AUTHORIZATION, generateJwt(account))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAccount)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @Test
    @DisplayName("정상적으로 유저가 삭제되는지 테스트")
    void 유저_삭제_테스트() throws Exception{
        Account account = saveAccount();

        mockMvc.perform(delete(accountUrl + "/{accountId}", account.getId())
                .header(HttpHeaders.AUTHORIZATION, generateJwt(account)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.login").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("delete-account",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("login").description("link to login"),
                                linkWithRel("profile").description("link to profile")
                        ), requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Authorization header")
                        ), responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), responseFields(
                                fieldWithPath("id").description("식별자"),
                                fieldWithPath("username").description("사용자가 사용할 이름"),
                                fieldWithPath("balance").description("사용자의 예치금"),
                                fieldWithPath("createdAt").description("생성 일시"),
                                fieldWithPath("ordersSet").description("사용자의 주문 리스트"),
                                fieldWithPath("productSet").description("사용자가 등록한 상품 리스트"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ));

        List<Account> all = accountRepository.findAll();
        assertEquals(all.size(), 0);
    }

    @Test
    @DisplayName("유저 삭제 시 삭제하려는 유저가 데이터베이스에 저장되어있지 않은 경우 Bad Request 반환")
    void 유저_삭제_저장안돼있을경우_테스트() throws Exception {
        Account account = saveAccount();

        mockMvc.perform(delete(accountUrl + "/{accountId}", -1)
                .header(HttpHeaders.AUTHORIZATION, generateJwt(account)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    @Test
    @DisplayName("정상적으로 유저가 조회되는지 테스트")
    void 유저_조회_테스트() throws Exception {
        Account savedAccount = saveAccount();

        mockMvc.perform(get(accountUrl + "/{accountId}", savedAccount.getId())
                .header(HttpHeaders.ACCEPT, MediaTypes.HAL_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, generateJwt(savedAccount)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("username").isNotEmpty())
                .andExpect(jsonPath("balance").isNotEmpty())
                .andExpect(jsonPath("createdAt").isNotEmpty())
                .andExpect(jsonPath("ordersSet").exists())
                .andExpect(jsonPath("productSet").exists())
                .andExpect(jsonPath("_links.update-account").exists())
                .andExpect(jsonPath("_links.delete-account").exists())
                .andExpect(jsonPath("_links.query-products").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-account",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("update-account").description("link to update account"),
                                linkWithRel("delete-account").description("link to delete account"),
                                linkWithRel("query-products").description("link to query products"),
                                linkWithRel("profile").description("link to profile")
                        ), requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Authorization header"),
                                headerWithName(HttpHeaders.ACCEPT).description("Accept header")
                        ), responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type header")
                        ), responseFields(
                                fieldWithPath("id").description("식별자"),
                                fieldWithPath("username").description("사용자가 사용할 이름"),
                                fieldWithPath("balance").description("사용자의 예치금"),
                                fieldWithPath("ordersSet").description("사용자의 주문 리스트"),
                                fieldWithPath("productSet").description("사용자가 등록한 상품 리스트"),
                                fieldWithPath("createdAt").description("생성 일시"),
                                fieldWithPath("_links.*.*").ignored()
                        )
                ));
    }

    @Test
    @DisplayName("조회하려는 유저가 데이터베이스에 저장되어 있지 않은 경우 Bad Request 반환")
    void 유저_조회_저장안돼있을경우_테스트() throws Exception {
        Account account = saveAccount();

        mockMvc.perform(get(accountUrl + "/{accountId}", -1)
                .header(HttpHeaders.AUTHORIZATION, generateJwt(account)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(KEY).exists());
    }

    private Account saveAccount() {
        return accountRepository.save(Account.builder()
                .username("username")
                .password("password")
                .balance(500000L)
                .createdAt(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .build());
    }
}