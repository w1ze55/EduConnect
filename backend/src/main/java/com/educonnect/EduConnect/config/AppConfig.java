package com.educonnect.EduConnect.config;

import com.educonnect.EduConnect.dto.RecadoDTO;
import com.educonnect.EduConnect.model.Recado;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        
        // Configurar mapeamento de Recado para RecadoDTO
        modelMapper.createTypeMap(Recado.class, RecadoDTO.class)
            .addMappings(mapper -> {
                mapper.skip(RecadoDTO::setRemetente);
                mapper.skip(RecadoDTO::setRemetenteId);
                mapper.skip(RecadoDTO::setLido);
                mapper.skip(RecadoDTO::setDataLeitura);
            });
        
        return modelMapper;
    }
}

