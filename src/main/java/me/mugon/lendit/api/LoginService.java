package me.mugon.lendit.api;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.config.jwt.JwtProvider;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.web.dto.JwtResponseDto;
import me.mugon.lendit.web.dto.LoginDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.mugon.lendit.api.error.ErrorMessageConstant.*;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;

    public ResponseEntity<?> login(LoginDto loginDto) {
        if (authenticate(loginDto.getUsername(), loginDto.getPassword())) {
            Map<String, List<String>> errors = new HashMap<>();
            errors.put(KEY, Arrays.asList(INVALIDIDORPASSWORD));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        String jwt = jwtProvider.generateToken(loginDto.toEntity());
        return new ResponseEntity<>(new JwtResponseDto(jwt), HttpStatus.OK);
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
