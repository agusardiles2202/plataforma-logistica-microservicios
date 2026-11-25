package tp.backend.ms_solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para representar una tarifa obtenida desde ms-tarifa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaDTO {
    private Integer idTarifa;
    private BigDecimal valorLitroCombustible;
    private BigDecimal cargoGestionPorTramo;
    private LocalDate fechaVigenciaDesde;
    private LocalDate fechaVigenciaHasta;
}
