package tp.backend.ms_solicitud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tp.backend.ms_solicitud.entity.Camion;

@Repository
public interface CamionRepository extends JpaRepository<Camion, String> {
}
