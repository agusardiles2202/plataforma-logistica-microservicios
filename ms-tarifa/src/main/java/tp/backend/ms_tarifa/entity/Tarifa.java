package tp.backend.ms_tarifa.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tarifa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa")
    private Integer idTarifa;
    
    @Column(name = "valor_litro_combustible", precision = 10, scale = 2)
    private BigDecimal valorLitroCombustible;

    @Column(name = "cargo_gestion_por_tramo", precision = 10, scale = 2)
    private BigDecimal cargoGestionPorTramo;

    @Column(name = "fecha_vigencia_desde")
    private LocalDate fechaVigenciaDesde;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate fechaVigenciaHasta;

}
