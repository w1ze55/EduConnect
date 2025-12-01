package com.educonnect.EduConnect.dto;

import lombok.Data;

import java.util.List;

@Data
public class DashboardResponseDTO {
    private DashboardStatsDTO stats;
    private List<RecadoResumoDTO> recadosRecentes;
    private List<AtividadeResumoDTO> atividadesPendentes;
    private List<EventoResumoDTO> proximosEventos;
}
