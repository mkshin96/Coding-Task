package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.config.jwt.JwtProvider;
import me.mugon.lendit.domain.common.BaseValidator;
import me.mugon.lendit.domain.login.LoginResource;
import me.mugon.lendit.web.ProductController;
import me.mugon.lendit.web.dto.JwtResponseDto;
import me.mugon.lendit.web.dto.LoginDto;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import static me.mugon.lendit.api.error.ErrorMessageConstant.INVALIDIDORPASSWORD;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;

    private final BaseValidator baseValidator;

    /**
     * 로그인
     * 1. 클라이언트에게 전달받은 Dto의 username과 password를 인증을 담당하는 AuthenticationManage로 넘김
     * 2. 검증에 실패하면 Body에 'message: 아이디 또는 비밀번호가 맞지 않습니다.'를 담아 Bad Request와 함께 반환
     * 3. 검증에 성공하면 jwt토큰 발급
     * 4. HATEOAS를 위해 query-products, self 관계를 EntityModel에 더함
     * 5. Self Descriptive Message를 위해 API Guide의 주소를 profile 관계로 명시하여 더함
     * 6. Header의 Location옵션에 생성된 유저를 조회할 수 있는 링크를 담고, Body에 위의 EntityModel을 실어 반환
     */
    public ResponseEntity<?> login(LoginDto loginDto) {
        if (authenticate(loginDto.getUsername(), loginDto.getPassword())) {
            return new ResponseEntity<>(baseValidator.returnErrorMessage(INVALIDIDORPASSWORD), HttpStatus.BAD_REQUEST);
        }
        String jwt = jwtProvider.generateToken(loginDto.toEntity());
        JwtResponseDto jwtResponseDto = new JwtResponseDto(jwt);
        LoginResource loginResource = new LoginResource(jwtResponseDto);
        loginResource.add(linkTo(ProductController.class).withRel("query-products"));
        loginResource.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-login-request").withRel("profile"));
        return new ResponseEntity<>(loginResource, HttpStatus.OK);
    }

    private boolean authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return false;
        }
        catch (DisabledException | BadCredentialsException e) {
            return true;
        }
    }
}
