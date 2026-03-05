package com.dev01.orderflow.orderapi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
        @Schema(
                description = "Novo status do pedido",
                example = "APPROVED",
                allowableValues = {"CREATED","APPROVED","CANCELED"}
        )
        @NotBlank String status
) {
}
