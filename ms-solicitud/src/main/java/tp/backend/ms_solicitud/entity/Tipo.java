package tp.backend.ms_solicitud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Integer id;

    @Column(name = "nombre", length = 50)
    private String nombre;

}
