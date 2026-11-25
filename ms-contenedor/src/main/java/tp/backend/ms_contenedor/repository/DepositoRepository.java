package tp.backend.ms_contenedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tp.backend.ms_contenedor.entity.Deposito;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {

}
