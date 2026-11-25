package tp.backend.ms_solicitud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tp.backend.ms_solicitud.entity.Cliente;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
