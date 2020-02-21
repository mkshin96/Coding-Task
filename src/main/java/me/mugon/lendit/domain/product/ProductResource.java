package me.mugon.lendit.domain.product;

import me.mugon.lendit.web.ProductController;
import me.mugon.lendit.web.dto.product.ProductResponseDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class ProductResource extends EntityModel<ProductResponseDto> {

    public ProductResource(ProductResponseDto responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(ProductController.class).slash(responseDto.getId()).withSelfRel());
    }
}
