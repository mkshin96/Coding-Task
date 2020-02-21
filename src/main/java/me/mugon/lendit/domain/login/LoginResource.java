package me.mugon.lendit.domain.login;

import me.mugon.lendit.web.LoginController;
import me.mugon.lendit.web.dto.JwtResponseDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class LoginResource extends EntityModel<JwtResponseDto> {

    public LoginResource(JwtResponseDto responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(LoginController.class).withSelfRel());
    }
}
