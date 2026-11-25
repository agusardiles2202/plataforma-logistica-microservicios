package tp.backend.ms_solicitud.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un tramo tentativo (no persistido en BD).
 * Se usa para calcular y devolver opciones de rutas al operador.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoTentativoDTO {
    
    private Integer orden;
    private String origenNombre;
    private String destinoNombre;
    private Integer origenDepositoId;  // ID del depósito si el origen es un depósito
    private Integer destinoDepositoId; // ID del depósito si el destino es un depósito
    private String tipo;
    private String dominio;  // Camión asignado (null en rutas tentativas, se asigna después)
    private BigDecimal distanciaKm;
    private String duracionTexto;
    private Long duracionEstimadaMinutos;
    private BigDecimal costoEstimado;
    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;
}
