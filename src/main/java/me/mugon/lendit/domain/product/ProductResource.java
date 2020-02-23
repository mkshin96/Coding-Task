package me.mugon.lendit.domain.product;

import me.mugon.lendit.web.ProductController;
import me.mugon.lendit.web.dto.product.ProductResponseDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * REST API의 규칙 중 하나인 HATEOAS를 만족시키기 위해 선언
 * 요청된 api인 자신을 나타내는 self 관계는 모든 api에 필요하기 때문에 코드 중복을 피하기 위해 다음과 같이 구현함.
 */
public class ProductResource extends EntityModel<ProductResponseDto> {

    public ProductResource(ProductResponseDto responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(ProductController.class).slash(responseDto.getId()).withSelfRel());
    }
}
