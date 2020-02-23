package me.mugon.lendit.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Orders 도메인을 데이터베이스와 매핑하여 CRUD 등의 작업을 하기 위해 선언
 */
public interface OrdersRepository extends JpaRepository<Orders, Long> {
}
