package com.educonnect.EduConnect.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificacaoLeituraRequest {
    @NotNull
    private Boolean lida;
}
