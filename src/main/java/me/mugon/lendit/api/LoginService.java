package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.config.jwt.JwtProvider;
import me.mugon.lendit.domain.BaseValidator;
import me.mugon.lendit.domain.login.LoginResource;
import me.mugon.lendit.web.ProductController;
import me.mugon.lendit.web.dto.JwtResponseDto;
import me.mugon.lendit.web.dto.LoginDto;
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

    public ResponseEntity<?> login(LoginDto loginDto) {
        if (authenticate(loginDto.getUsername(), loginDto.getPassword())) {
            return new ResponseEntity<>(baseValidator.returnErrorMessage(INVALIDIDORPASSWORD), HttpStatus.BAD_REQUEST);
        }
        String jwt = jwtProvider.generateToken(loginDto.toEntity());
        JwtResponseDto jwtResponseDto = new JwtResponseDto(jwt);
        LoginResource loginResource = new LoginResource(jwtResponseDto);
        loginResource.add(linkTo(ProductController.class).withRel("query-products"));
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
