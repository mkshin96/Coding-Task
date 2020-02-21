package me.mugon.lendit.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.mugon.lendit.config.jwt.JwtConstants;
import me.mugon.lendit.config.jwt.JwtProvider;
import me.mugon.lendit.domain.account.Account;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class BaseControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public JwtProvider jwtProvider;

    public String generateJwt(Account account) {
        return JwtConstants.TOKEN_PREFIX + jwtProvider.generateToken(account);
    }
}
