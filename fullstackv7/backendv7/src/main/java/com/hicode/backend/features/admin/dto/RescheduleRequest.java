package com.hicode.backend.features.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RescheduleRequest {
    @NotBlank(message = "Reason for rescheduling is required.")
    private String reason;
}