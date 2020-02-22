package me.mugon.lendit.web;

import lombok.RequiredArgsConstructor;
import me.mugon.lendit.api.AccountService;
import me.mugon.lendit.domain.common.BaseValidator;
import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.account.CurrentUser;
import me.mugon.lendit.web.dto.account.AccountRequestDto;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping(value = "/api/accounts", produces = MediaTypes.HAL_JSON_VALUE)
@RestController
public class AccountController {

    private final AccountService accountService;

    private final BaseValidator baseValidator;

    @PostMapping
    public ResponseEntity<?> saveAccount(@Valid @RequestBody AccountRequestDto requestDto, Errors errors) {
        if (errors.hasErrors()) {
            return baseValidator.returnErrors(errors);
        }
        return accountService.saveAccount(requestDto);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<?> updateAccount(@PathVariable Long accountId, @Valid @RequestBody AccountRequestDto requestDto, Errors errors, @CurrentUser Account currentUser) {
        if (errors.hasErrors()) {
            return baseValidator.returnErrors(errors);
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
}
