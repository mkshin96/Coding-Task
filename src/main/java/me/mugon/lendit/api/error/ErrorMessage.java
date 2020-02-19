package me.mugon.lendit.api.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter @AllArgsConstructor
public class ErrorMessage {

    private Map<String, List<String>> message;
}
