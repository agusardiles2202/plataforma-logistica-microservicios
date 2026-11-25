package tp.backend.ms_solicitud.repository;

import tp.backend.ms_solicitud.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Integer> {
    // Ya no necesitamos findBySolicitud_Id porque ahora Solicitud tiene la FK
    // Para obtener la ruta de una solicitud, accedemos directamente a solicitud.getRuta()
}