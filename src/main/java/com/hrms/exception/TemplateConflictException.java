package com.hrms.exception;

import com.hrms.dto.response.ConflictingTemplate;
import lombok.Getter;
import java.util.List;

@Getter
public class TemplateConflictException extends RuntimeException {

    private final List<ConflictingTemplate> conflicts;

    public TemplateConflictException(String message, List<ConflictingTemplate> conflicts) {
        super(message);
        this.conflicts = conflicts;
    }
}
