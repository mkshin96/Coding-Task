package me.mugon.lendit.domain.order;

import me.mugon.lendit.web.dto.order.OrdersResponseDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

/**
 * REST API의 규칙 중 하나인 HATEOAS를 만족시키기 위해 선언
 * 현재 OrdersController의 api는 List를 반환하기 때문에 일반적인 EntityModel<?>는 Spring HATEOAS의 HAL Serializer를 관여하지 않음
 * 따라서 Spring HATEOAS의 HAL Serializer를 사용하기 위해 다음과 같이 구현
 * Reference
 * https://github.com/spring-projects/spring-hateoas/issues/709
 * https://docs.spring.io/spring-hateoas/docs/1.0.3.RELEASE/reference/html/
 */
public class OrdersResource extends CollectionModel<EntityModel<OrdersResponseDto>> {

    public OrdersResource(Iterable<EntityModel<OrdersResponseDto>> entityModels, Link... links) {
        super(entityModels, links);
    }
}
