package com.hicode.backend.features.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBloodCompatibilityRequest {
    private Boolean isCompatible;
    private Integer compatibilityScore;
    private Boolean isEmergencyCompatible;
    private String notes;
}