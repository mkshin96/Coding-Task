package me.mugon.lendit.domain.product;

import me.mugon.lendit.web.dto.product.ProductResponseDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class ProductListResource extends CollectionModel<EntityModel<ProductResponseDto>> {

    public ProductListResource(Iterable<EntityModel<ProductResponseDto>> entityModels, Link... links) {
        super(entityModels, links);
    }
}
