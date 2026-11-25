package tp.backend.ms_solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para encapsular los datos necesarios para calcular costos de rutas.
 * Incluye tarifa de combustible y promedios calculados de los camiones disponibles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosCostoDTO {
    
    /**
     * Valor por litro de combustible (desde Tarifa vigente)
     */
    private BigDecimal valorLitroCombustible;
    
    /**
     * Consumo promedio de combustible en litros por kilómetro (promedio de todos los camiones)
     */
    private BigDecimal consumoPromedioPorKm;
    
    /**
     * Costo base por kilómetro (promedio de todos los camiones)
     */
    private BigDecimal costoBasePorKm;
}
