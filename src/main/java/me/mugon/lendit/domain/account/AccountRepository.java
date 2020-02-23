package me.mugon.lendit.domain.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Account 도메인을 데이터베이스와 매핑하여 CRUD 등의 작업을 하기 위해 선언
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
}
