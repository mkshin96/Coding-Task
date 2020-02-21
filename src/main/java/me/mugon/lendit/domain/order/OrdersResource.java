package me.mugon.lendit.domain.order;

import me.mugon.lendit.web.OrdersController;
import me.mugon.lendit.web.dto.order.OrdersResponseDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class OrdersResource extends EntityModel<OrdersResponseDto> {

    public OrdersResource(OrdersResponseDto responseDto, Link... links) {
        super(responseDto, links);
        add(linkTo(OrdersController.class).slash(responseDto.getId()).withSelfRel());
    }
}
