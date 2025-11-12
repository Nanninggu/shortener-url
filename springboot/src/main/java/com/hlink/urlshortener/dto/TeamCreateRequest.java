package com.hlink.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeamCreateRequest {
    @NotBlank(message = "팀 이름은 필수입니다")
    @Size(min = 2, max = 100, message = "팀 이름은 2-100자 사이여야 합니다")
    private String name;
}

