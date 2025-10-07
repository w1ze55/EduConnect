package com.educonnect.EduConnect.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtribuirDiretorDTO {
    
    @NotNull(message = "ID da escola é obrigatório")
    private Long escolaId;
    
    @NotNull(message = "ID do diretor é obrigatório")
    private Long diretorId;
}
