package tp.backend.ms_solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para devolver el resultado completo del cálculo de rutas tentativas.
 * Contiene todas las opciones de rutas calculadas para una solicitud.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutasTentativasResponseDTO {
    
    private Integer solicitudId;                    // ID de la solicitud
    private String origen;                          // Origen de la solicitud
    private String destino;                         // Destino de la solicitud
    private Integer cantidadOpcionesCalculadas;     // Cantidad de rutas calculadas
    private List<RutaTentativaDTO> rutas;           // Lista de rutas tentativas
    private String mensaje;                         // Mensaje informativo
}
