package me.mugon.lendit.web;

import me.mugon.lendit.api.error.ErrorMessageConstant;
import me.mugon.lendit.common.BaseControllerTest;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.AccountRepository;
import me.mugon.lendit.domain.account.Role;
import me.mugon.lendit.web.dto.LoginDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginControllerTest extends BaseControllerTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    private final String loginUrl = "/api/login";
    private final String username = "username";
    private final String password = "password";

    @AfterEach
    void clean() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적으로 로그인 성공")
    void 로그인() throws Exception {
        Account account = Account.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .balance(5000L)
                .createdAt(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .build();

        Account savedAccount = accountRepository.save(account);

        assertEquals(savedAccount.getUsername(), username);
        assertEquals(savedAccount.getBalance(), 5000L);
        assertTrue(savedAccount.getCreatedAt().isBefore(LocalDateTime.now()));

        LoginDto loginDto = LoginDto.builder()
                .username(username)
                .password(password)
                .build();

        mockMvc.perform(post(loginUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").exists());
    }

    @Test
    @DisplayName("데이터베이스에 존재하지 않는 아이디일 경우")
    void 로그인_실패_because_아이디() throws Exception {
        LoginDto loginDto = LoginDto.builder()
                .username("unRegisteredUser")
                .password(password)
                .build();

        mockMvc.perform(post(loginUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ErrorMessageConstant.KEY).exists());
    }

    @Test
    @DisplayName("아이디는 맞지만 비밀번호가 틀린 경우")
    void 로그인_실패_because_비밀번호() throws Exception {
        Account account = Account.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .balance(5000L)
                .createdAt(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .build();

        Account savedAccount = accountRepository.save(account);

        assertEquals(savedAccount.getUsername(), username);
        assertEquals(savedAccount.getBalance(), 5000L);
        assertTrue(savedAccount.getCreatedAt().isBefore(LocalDateTime.now()));

        LoginDto loginDto = LoginDto.builder()
                .username(username)
                .password("invalidPassword")
                .build();

        mockMvc.perform(post(loginUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(ErrorMessageConstant.KEY).exists());
    }
}