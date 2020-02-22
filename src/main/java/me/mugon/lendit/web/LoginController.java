package me.mugon.lendit.web;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.LoginService;
import me.mugon.lendit.domain.BaseValidator;
import me.mugon.lendit.web.dto.LoginDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping(value = "/api/login")
@RestController
public class LoginController {

    private final LoginService loginService;

    private final BaseValidator baseValidator;

    @PostMapping
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(baseValidator.returnErrors(errors), HttpStatus.BAD_REQUEST);
        }
        return loginService.login(loginDto);
    }
}
