package tp.backend.ms_solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorDTO {
    private Integer idContenedor;
    private BigDecimal peso;
    private BigDecimal volumen;
}
