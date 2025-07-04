package com.hicode.backend.features.admin.dto;

import com.hicode.backend.features.user.dto.UserResponse;
import com.hicode.backend.model.enums.DonationStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class DonationProcessResponse {
    private Long id;
    private UserResponse donor;
    private DonationStatus status;
    private String note;
    private Integer collectedVolumeMl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AppointmentResponse appointment;
}