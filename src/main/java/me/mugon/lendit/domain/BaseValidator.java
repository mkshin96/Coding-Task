package me.mugon.lendit.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.*;

import static me.mugon.lendit.api.error.ErrorMessageConstant.KEY;

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
