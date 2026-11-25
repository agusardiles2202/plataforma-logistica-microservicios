package tp.backend.ms_solicitud.geo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para almacenar coordenadas geográficas obtenidas de Google Geocoding API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadasDTO {
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String direccionFormateada;
}
