package tp.backend.ms_solicitud.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tramo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tramo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origen", referencedColumnName = "id_punto", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Punto origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino", referencedColumnName = "id_punto", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Punto destino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo", referencedColumnName = "id_tipo", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Tipo tipo;

    @Column(name = "costo_aproximado", precision = 12, scale = 2)
    private BigDecimal costoAproximado;

    @Column(name = "costo_real", precision = 12, scale = 2)
    private BigDecimal costoReal;

    @Column(name = "fecha_hora_inicio_estimada")
    private LocalDateTime fechaHoraInicioEstimada;

    @Column(name = "fecha_hora_fin_estimada")
    private LocalDateTime fechaHoraFinEstimada;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @Column(name = "kilometros_tramo", precision = 10, scale = 2)
    private BigDecimal kilometrosTramo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dominio", referencedColumnName = "dominio")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Camion camion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta", referencedColumnName = "id_ruta", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "solicitud", "tramos"})
    private Ruta ruta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_tramo", nullable = false, length = 20)
    private EstadoTramoEnum estado;

}
