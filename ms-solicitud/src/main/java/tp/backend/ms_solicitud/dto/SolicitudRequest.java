package tp.backend.ms_solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tp.backend.ms_solicitud.entity.Cliente;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRequest {
    private ContenedorData contenedor; // OBLIGATORIO: datos del contenedor a crear
    private String origen;
    private String destino;
    private Cliente cliente;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContenedorData {
        private Double peso;
        private Double volumen;
    }
}
