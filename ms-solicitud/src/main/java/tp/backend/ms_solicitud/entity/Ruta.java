package tp.backend.ms_solicitud.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ruta")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Integer id;

    // Relación: una solicitud tiene UNA SOLA ruta (OneToOne bidireccional)
    // Ruta NO es owner de la relación (mappedBy apunta al campo "ruta" en Solicitud)
    @OneToOne(mappedBy = "ruta", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"ruta", "solicitud"})
    private Solicitud solicitud;

    @Column(name = "cantidad_tramos", nullable = false)
    private Integer cantidadTramos;

    @Column(name = "cantidad_depositos", nullable = false)
    private Integer cantidadDepositos;

    // Relación: una ruta tiene muchos tramos (OneToMany)
    @OneToMany(mappedBy = "ruta", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"ruta", "hibernateLazyInitializer", "handler"})
    private List<Tramo> tramos;
}
