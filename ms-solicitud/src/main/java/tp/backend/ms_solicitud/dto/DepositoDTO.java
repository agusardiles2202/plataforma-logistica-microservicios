package tp.backend.ms_solicitud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar un depósito obtenido desde ms-contenedor.
 * Usado para calcular rutas tentativas con depósitos intermedios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositoDTO {
    
    @JsonProperty("idDeposito")
    private Long idDeposito;
    
    private String nombre;
    
    private String direccion;
    
    @JsonProperty("costoEstadiaDiario")
    private BigDecimal costoEstadiaDiario;
    
    // Campos opcionales para coordenadas (si se agregan en el futuro)
    private BigDecimal latitud;
    
    private BigDecimal longitud;
    
    // Alias para compatibilidad con código existente
    public Long getId() {
        return idDeposito;
    }
    
    public void setId(Long id) {
        this.idDeposito = id;
    }
    
    public BigDecimal getCostoDiaDeposito() {
        return costoEstadiaDiario;
    }
    
    public void setCostoDiaDeposito(BigDecimal costo) {
        this.costoEstadiaDiario = costo;
    }
}


