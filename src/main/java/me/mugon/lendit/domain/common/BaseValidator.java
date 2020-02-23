package me.mugon.lendit.domain.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.*;

import static me.mugon.lendit.api.error.ErrorMessageConstant.KEY;

/**
 * 프론트엔드와의 협업 시 유효성에 문제가 있을 경우 같은 형식으로 반환하기 위해 다음과 같이 구현
 * 모든 Controller, Service 클래스에서 사용되기 때문에 코드 중복을 피하기 위해 하나의 클래스에 구현 후 사용함
 */
@Component
public class BaseValidator {

    public ResponseEntity<?> returnErrors(Errors errors) {
        Map<String, List<String>> errorMap = new HashMap<>();
        errors.getFieldErrors().forEach(e -> {
            if (errorMap.get(KEY) != null) {
                errorMap.get(KEY).add(e.getField() + " 가 유효하지 않습니다.");
            } else {
                List<String> list = new LinkedList<>();
                list.add(e.getField() + " 가 유효하지 않습니다.");
                errorMap.put(KEY, list);
            }
        });

        errors.getGlobalErrors().forEach(e -> {
            if (errorMap.get(KEY) != null) {
                errorMap.get(KEY).add(e.getObjectName() + " 가 유효하지 않습니다.");
            } else {
                List<String> list = new LinkedList<>();
                list.add(e.getObjectName() + " 가 유효하지 않습니다.");
                errorMap.put(KEY, list);
            }
        });
        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }

    public Map<String, List<String>> returnErrorMessage(String message) {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(KEY, Arrays.asList(message));
        return errors;
    }
}
