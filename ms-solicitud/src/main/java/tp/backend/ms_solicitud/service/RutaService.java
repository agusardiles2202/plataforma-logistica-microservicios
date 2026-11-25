package tp.backend.ms_solicitud.service;

import tp.backend.ms_solicitud.entity.Ruta;
import tp.backend.ms_solicitud.repository.RutaRepository;
import tp.backend.ms_solicitud.repository.SolicitudRepository;
import tp.backend.ms_solicitud.entity.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RutaService {

    private final RutaRepository rutaRepository;
    private final SolicitudRepository solicitudRepository;

    public List<Ruta> findAll() {
        return rutaRepository.findAll();
    }

    public Ruta findById(Integer id) {
        return rutaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ruta no encontrada con id " + id));
    }

    @Transactional
    public Ruta create(Ruta ruta) {
        ruta.setId(null);
        // Esperamos que el objeto ruta traiga referencia a solicitud con el id seteado
        if (ruta.getSolicitud() == null || ruta.getSolicitud().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitud (id) es requerida para crear una Ruta");
        }
        Integer solicitudId = ruta.getSolicitud().getId();
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada con id " + solicitudId));
        
        // Primero guardar la ruta (sin relación bidireccional todavía)
        Ruta rutaGuardada = rutaRepository.save(ruta);
        
        // Luego asignar la ruta a la solicitud (owner de la relación)
        solicitud.setRuta(rutaGuardada);
        solicitudRepository.save(solicitud);
        
        return rutaGuardada;
    }

    @Transactional
    public Ruta update(Integer id, Ruta ruta) {
        Ruta existente = rutaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ruta no encontrada con id " + id));
        
        // Actualizar la referencia a Solicitud si se provee
        if (ruta.getSolicitud() != null && ruta.getSolicitud().getId() != null) {
            Integer solicitudId = ruta.getSolicitud().getId();
            Solicitud solicitud = solicitudRepository.findById(solicitudId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada con id " + solicitudId));
            
            // Asignar la ruta a la solicitud (owner de la relación)
            solicitud.setRuta(existente);
            solicitudRepository.save(solicitud);
        }
        
        existente.setCantidadTramos(ruta.getCantidadTramos());
        existente.setCantidadDepositos(ruta.getCantidadDepositos());
        return rutaRepository.save(existente);
    }

    @Transactional
    public void delete(Integer id) {
        if (!rutaRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ruta no encontrada con id " + id);
        }
        rutaRepository.deleteById(id);
    }

    /**
     * Encuentra la ruta única asociada a una solicitud.
     * @param solicitudId ID de la solicitud
     * @return La ruta asociada a la solicitud, o null si no tiene ruta asignada
     */
    public Ruta findBySolicitud(Integer solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitud no encontrada con id " + solicitudId));
        return solicitud.getRuta();
    }
}
