package me.mugon.lendit.domain.order;

import me.mugon.lendit.domain.account.Account;
import me.mugon.lendit.domain.common.BaseValidator;
import me.mugon.lendit.domain.product.Product;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import javax.validation.Validation;
import java.util.Collection;

/**
 * Collection 을 @Valid 로 유효성 검증을 하기 위해 Validator를 implement 받아 구현함
 * Reference
 * https://gompangs.tistory.com/entry/Spring-Valid-Collection-Validation-%EA%B4%80%EB%A0%A8
 */
@Component
public class OrdersValidator extends BaseValidator implements Validator {

    private final SpringValidatorAdapter validatorAdapter;

    public OrdersValidator() {
        this.validatorAdapter = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());
    }

    public boolean isValidUser(Account currentUser, Product product) {
        return currentUser.getId().equals(product.getAccount().getId());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof Collection) {
            Collection collection = (Collection) target;
            for (Object object : collection) {
                validatorAdapter.validate(object, errors);
            }
        } else {
            validatorAdapter.validate(target, errors);
        }
    }
}
