package me.mugon.lendit.web;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.LoginService;
import me.mugon.lendit.web.dto.LoginDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static me.mugon.lendit.api.error.ErrorMessageConstant.KEY;

@RequiredArgsConstructor
@RequestMapping(value = "/api/login")
@RestController
public class LoginController {

    private final LoginService loginService;

    @PostMapping
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity<>(returnError(errors), HttpStatus.BAD_REQUEST);
        }
        return loginService.login(loginDto);
    }

    private ResponseEntity<?> returnError(Errors errors) {
        Map<String, List<String>> errorMap = new HashMap<>();
        errors.getFieldErrors().forEach(e -> {
            if (errorMap.get(KEY) != null) {
                errorMap.get(KEY).add(e.getField() + " 가 유효하지 않습니다.");
            } else {
                List<String> list = new LinkedList<>();
                list.add(e.getField() + " 가 유효하지 않습니다.");
                errorMap.put(KEY, list);
            }
        });

        errors.getGlobalErrors().forEach(e -> {
            if (errorMap.get(KEY) != null) {
                errorMap.get(KEY).add(e.getObjectName() + " 가 유효하지 않습니다.");
            } else {
                List<String> list = new LinkedList<>();
                list.add(e.getObjectName() + " 가 유효하지 않습니다.");
                errorMap.put(KEY, list);
            }
        });
        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }
}
