package tp.backend.ms_solicitud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @Column(name = "dni_cliente")
    private Integer dniCliente;

    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre;

    @Column(name = "apellido", length = 120, nullable = false)
    private String apellido;

    @Column(name = "telefono", length = 20)
    private String telefono;

}
