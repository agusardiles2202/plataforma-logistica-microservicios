package tp.backend.ms_contenedor.entity;

import java.math.BigDecimal;

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
@Table(name = "deposito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_deposito")
    private Long idDeposito;

    @Column(name = "nombre", length = 128)
    private String nombre;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "costo_estadia_diario", precision = 10, scale = 2, nullable = true)
    private BigDecimal costoEstadiaDiario;

}
