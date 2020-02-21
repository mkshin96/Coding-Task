package me.mugon.lendit.web;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.AccountService;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.CurrentUser;
import me.mugon.lendit.web.dto.account.AccountRequestDto;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static me.mugon.lendit.api.error.ErrorMessageConstant.KEY;

@RequiredArgsConstructor
@RequestMapping(value = "/api/accounts", produces = MediaTypes.HAL_JSON_VALUE)
@RestController
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> saveAccount(@Valid @RequestBody AccountRequestDto requestDto, Errors errors) {
        if (errors.hasErrors()) {
            return returnError(errors);
        }
        return accountService.saveAccount(requestDto);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<?> updateAccount(@PathVariable Long accountId, @Valid @RequestBody AccountRequestDto requestDto, Errors errors, @CurrentUser Account currentUser) {
        if (errors.hasErrors()) {
            return returnError(errors);
        }
        return accountService.updateAccount(accountId, requestDto);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long accountId, @CurrentUser Account currentUser) {
        return accountService.deleteAccount(accountId);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccount(@PathVariable Long accountId, @CurrentUser Account currentUser) {
        return accountService.getAccount(accountId);
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
