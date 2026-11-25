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
public class CamionContenedorCompatibilidadDTO {
    
    private boolean puedeTransportar;
    private CamionInfo camion;
    private ContenedorInfo contenedor;
    private Restricciones restricciones;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CamionInfo {
        private String dominio;
        private BigDecimal capacidadPeso;
        private BigDecimal capacidadVolumen;
        private BigDecimal costoBasePorKm;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContenedorInfo {
        private Integer id;
        private BigDecimal peso;
        private BigDecimal volumen;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Restricciones {
        private boolean cumplePeso;
        private boolean cumpleVolumen;
        private String mensaje;
    }
}
