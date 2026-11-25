package tp.backend.ms_solicitud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tp.backend.ms_solicitud.entity.EstadoSolicitudEnum;
import tp.backend.ms_solicitud.entity.Solicitud;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {

    List<Solicitud> findByClienteId(Integer clienteId);
    List<Solicitud> findByEstado(EstadoSolicitudEnum estado);
    Solicitud findByRutaId(Integer rutaId);
}
