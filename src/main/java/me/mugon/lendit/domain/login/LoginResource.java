package me.mugon.lendit.domain.login;

import me.mugon.lendit.web.LoginController;
import me.mugon.lendit.web.dto.JwtResponseDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * REST API의 규칙 중 하나인 HATEOAS를 만족시키기 위해 선언
 * 요청된 api인 자신을 나타내는 self 관계는 모든 api에 필요하기 때문에 코드 중복을 피하기 위해 다음과 같이 구현함.
 */
public class LoginResource extends EntityModel<JwtResponseDto> {

    public LoginResource(JwtResponseDto responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(LoginController.class).withSelfRel());
    }
}
