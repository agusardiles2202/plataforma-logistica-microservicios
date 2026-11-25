package tp.backend.ms_solicitud.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "camion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    // dominio es la PK (matrícula)
    @Id
    @Column(name = "dominio", length = 10)
    private String dominio;

    @Column(name = "nombre_transportista", length = 120)
    private String nombreTransportista;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "capacidad_peso", precision = 10, scale = 2)
    private BigDecimal capacidadPeso;

    @Column(name = "capacidad_volumen", precision = 10, scale = 2)
    private BigDecimal capacidadVolumen;

    @Column(name = "disponibilidad")
    private Boolean disponibilidad;

    @Column(name = "consumo_por_kilometro", precision = 10, scale = 3)
    private BigDecimal consumoPorKilometro;

    @Column(name = "costo_base_por_km", precision = 10, scale = 2)
    private BigDecimal costoBasePorKm;


}
