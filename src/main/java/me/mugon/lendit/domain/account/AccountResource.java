package me.mugon.lendit.domain.account;

import me.mugon.lendit.web.AccountController;
import me.mugon.lendit.web.dto.account.AccountResponseDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class AccountResource extends EntityModel<AccountResponseDto> {

    public AccountResource(AccountResponseDto responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(AccountController.class).slash(responseDto.getId()).withSelfRel());
    }
}
