
package tp.backend.ms_solicitud.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer id;

    @Column(name = "contenedor", nullable = false)
    private Integer contenedorId;

    @Column(name = "cliente", nullable = false)
    private Integer clienteId;

    @Column(name = "costo_estimado")
    private Double costoEstimado;

    @Column(name = "tiempo_estimado")
    private java.sql.Date tiempoEstimado;

    @Column(name = "costo_final")
    private Double costoFinal;

    @Column(name = "tiempo_real")
    private java.sql.Date tiempoReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false, length = 20)
    private EstadoSolicitudEnum estado = EstadoSolicitudEnum.BORRADOR;

    @Column(name = "origen")
    private String origen;

    @Column(name = "destino")
    private String destino;

    // Relación: una solicitud tiene UNA SOLA ruta (OneToOne)
    // Solicitud es el owner de la relación (tiene la FK ruta en la BD)
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ruta", referencedColumnName = "id_ruta", unique = true)
    @JsonIgnoreProperties({"solicitud", "hibernateLazyInitializer", "handler"})
    private Ruta ruta;
}
