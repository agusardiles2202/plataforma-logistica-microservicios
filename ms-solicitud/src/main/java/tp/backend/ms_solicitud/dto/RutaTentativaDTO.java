package tp.backend.ms_solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaTentativaDTO {
    
    private Integer numeroOpcion;               
    private Integer cantidadTramos;             
    private Integer cantidadDepositos;          
    private BigDecimal distanciaTotalKm;       
    private String duracionTotalTexto;          
    private BigDecimal costoTotalEstimado;      
    private List<TramoTentativoDTO> tramos;     
    private String descripcion;                 
}
