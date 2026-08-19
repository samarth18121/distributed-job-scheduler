package com.jobSchedular.p.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateJobRequest {

    @NotBlank
    private String jobType;
    private String payload;
    @Min(1)
    private int maxAttempts;
}
