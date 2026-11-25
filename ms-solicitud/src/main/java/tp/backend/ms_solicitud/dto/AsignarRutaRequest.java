package tp.backend.ms_solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para recibir la ruta seleccionada por el operador y persistirla en BD.
 * El operador elige una de las rutas tentativas calculadas y envía este request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarRutaRequest {
    
    private Integer numeroOpcionSeleccionada;  // Número de opción elegida (1, 2, 3...)
    private List<TramoParaPersistir> tramos;    // Lista de tramos a crear
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TramoParaPersistir {
        private Integer orden;
        private String origenNombre;    // Nombre del punto origen (puede ser dirección)
        private String destinoNombre;   // Nombre del punto destino
        private Integer tipoId;         // ID del tipo de tramo
        private Double distanciaKm;
        private String duracionTexto;
        private Double costoEstimado;
    }
}
