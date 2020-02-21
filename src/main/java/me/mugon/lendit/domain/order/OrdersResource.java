package me.mugon.lendit.domain.order;

import me.mugon.lendit.web.dto.order.OrdersResponseDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class OrdersResource extends CollectionModel<EntityModel<OrdersResponseDto>> {

    public OrdersResource(Iterable<EntityModel<OrdersResponseDto>> entityModels, Link... links) {
        super(entityModels, links);
    }
}
