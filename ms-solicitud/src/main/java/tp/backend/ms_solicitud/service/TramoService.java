package tp.backend.ms_solicitud.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tp.backend.ms_solicitud.client.ContenedorClientService;
import tp.backend.ms_solicitud.dto.ContenedorDTO;
import tp.backend.ms_solicitud.entity.Camion;
import tp.backend.ms_solicitud.entity.EstadoSolicitudEnum;
import tp.backend.ms_solicitud.entity.EstadoTramoEnum;
import tp.backend.ms_solicitud.entity.Solicitud;
import tp.backend.ms_solicitud.entity.Tramo;
import tp.backend.ms_solicitud.repository.SolicitudRepository;
import tp.backend.ms_solicitud.repository.TramoRepository;

@Service
@RequiredArgsConstructor
public class TramoService {

    private final TramoRepository tramoRepository;
    private final CamionService camionService;
    private final SolicitudRepository solicitudRepository;
    private final ContenedorClientService contenedorClientService;

    public List<Tramo> findAll() {
        return tramoRepository.findAll();
    }

    public Tramo findById(Integer id) {
        return tramoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramo no encontrado"));
    }

    public Tramo create(Tramo tramo) {
        // Si el operador especificó un camión, validar que esté disponible
        if (tramo.getCamion() != null) {
            validarDisponibilidadCamion(tramo.getCamion().getDominio());
        }
        return tramoRepository.save(tramo);
    }

    public Tramo update(Integer id, Tramo tramo) {
        Tramo existente = tramoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramo no encontrado"));

        // copiar campos no nulos (relaciones y valores simples)
        if (tramo.getOrigen() != null) existente.setOrigen(tramo.getOrigen());
        if (tramo.getDestino() != null) existente.setDestino(tramo.getDestino());
        if (tramo.getTipo() != null) existente.setTipo(tramo.getTipo());
        if (tramo.getCostoAproximado() != null) existente.setCostoAproximado(tramo.getCostoAproximado());
        if (tramo.getCostoReal() != null) existente.setCostoReal(tramo.getCostoReal());
        if (tramo.getFechaHoraInicio() != null) existente.setFechaHoraInicio(tramo.getFechaHoraInicio());
        if (tramo.getFechaHoraFin() != null) existente.setFechaHoraFin(tramo.getFechaHoraFin());
        if (tramo.getKilometrosTramo() != null) existente.setKilometrosTramo(tramo.getKilometrosTramo());
        if (tramo.getCamion() != null) {
            validarDisponibilidadCamion(tramo.getCamion().getDominio());
            existente.setCamion(tramo.getCamion());
        }
        if (tramo.getRuta() != null) existente.setRuta(tramo.getRuta());
        if (tramo.getEstado() != null) existente.setEstado(tramo.getEstado());

        return tramoRepository.save(existente);
    }

    public void delete(Integer id) {
        if (!tramoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramo no encontrado");
        }
        tramoRepository.deleteById(id);
    }

    public Tramo registrarInicio(Integer id, java.time.LocalDateTime horaInicio) {
        Tramo existente = tramoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramo no encontrado"));

        if (existente.getFechaHoraInicio() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inicio ya registrado");
        }

        existente.setFechaHoraInicio(horaInicio);
        existente.setEstado(EstadoTramoEnum.INICIADO); // Cambiar estado a INICIADO
        Tramo tramoActualizado = tramoRepository.save(existente);
        
        // Actualizar estado de la solicitud a EN_TRANSITO cuando inicia el primer tramo
        actualizarEstadoSolicitudSiCorresponde(existente.getRuta().getId());
        
        return tramoActualizado;
    }

    public Tramo registrarFin(Integer id, java.time.LocalDateTime horaFin) {
        Tramo existente = tramoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tramo no encontrado"));

        if (existente.getFechaHoraInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede registrar fin sin inicio previo");
        }

        if (existente.getFechaHoraFin() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Fin ya registrado");
        }

        existente.setFechaHoraFin(horaFin);
        existente.setEstado(EstadoTramoEnum.FINALIZADO); // Cambiar estado a FINALIZADO
        Tramo tramoActualizado = tramoRepository.save(existente);
        
        // Verificar si todos los tramos están finalizados para marcar solicitud como ENTREGADA
        verificarYMarcarSolicitudEntregada(existente.getRuta().getId());
        
        return tramoActualizado;
    }

    /**
     * Asigna un camión a un tramo y cambia su estado a ASIGNADO.
     * Valida que el camión exista y esté disponible.
     * 
     * @param tramoId ID del tramo
     * @param dominioCamion Dominio del camión a asignar
     * @return Tramo actualizado con camión asignado y estado ASIGNADO
     */
    public Tramo asignarCamion(Integer tramoId, String dominioCamion) {
        Tramo tramo = findById(tramoId);
        
        // Validar que el camión existe y está disponible
        validarDisponibilidadCamion(dominioCamion);
        Camion camion = camionService.findById(dominioCamion);
        
        // Validar capacidad del camión vs contenedor
        validarCapacidadCamion(tramo, camion);
        
        // Asignar camión y cambiar estado
        tramo.setCamion(camion);
        tramo.setEstado(EstadoTramoEnum.ASIGNADO);
        
        return tramoRepository.save(tramo);
    }

    /**
     * Lista todos los tramos asignados a un camión (por dominio).
     */
    public List<Tramo> findByCamionDominio(String dominio) {
        return tramoRepository.findByCamionDominio(dominio);
    }

    /**
     * Lista todos los tramos de una ruta específica.
     */
    public List<Tramo> findByRutaId(Integer rutaId) {
        return tramoRepository.findByRutaId(rutaId);
    }

    /**
     * Valida que el camión exista y esté disponible antes de asignarlo a un tramo.
     * Lanza ResponseStatusException si no está disponible o no existe.
     */
    private void validarDisponibilidadCamion(String dominio) {
        Camion camion = camionService.findById(dominio); // lanza 404 si no existe
        
        if (camion.getDisponibilidad() == null || !camion.getDisponibilidad()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Camion no disponible para asignar: " + dominio);
        }
    }

    /**
     * Valida que el camión tenga suficiente capacidad para el contenedor de la solicitud.
     * Lanza ResponseStatusException si no cumple con los requisitos de peso o volumen.
     */
    private void validarCapacidadCamion(Tramo tramo, Camion camion) {
        // Obtener la solicitud asociada al tramo
        Solicitud solicitud = solicitudRepository.findByRutaId(tramo.getRuta().getId());
        
        if (solicitud == null || solicitud.getContenedorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "No se puede validar capacidad: solicitud o contenedor no encontrado");
        }
        
        // Obtener el contenedor del microservicio
        ContenedorDTO contenedor = contenedorClientService.obtenerContenedor(solicitud.getContenedorId());
        
        if (contenedor == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "No se puede validar capacidad: contenedor no encontrado");
        }
        
        // Validar peso
        if (contenedor.getPeso() != null && camion.getCapacidadPeso() != null) {
            if (contenedor.getPeso().compareTo(camion.getCapacidadPeso()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    String.format("El camión %s no tiene suficiente capacidad de peso. Requerido: %.2f kg, Disponible: %.2f kg", 
                        camion.getDominio(), contenedor.getPeso(), camion.getCapacidadPeso()));
            }
        }
        
        // Validar volumen
        if (contenedor.getVolumen() != null && camion.getCapacidadVolumen() != null) {
            if (contenedor.getVolumen().compareTo(camion.getCapacidadVolumen()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    String.format("El camión %s no tiene suficiente capacidad de volumen. Requerido: %.2f m³, Disponible: %.2f m³", 
                        camion.getDominio(), contenedor.getVolumen(), camion.getCapacidadVolumen()));
            }
        }
    }

    /**
     * Actualiza el estado de la solicitud a EN_TRANSITO cuando inicia el primer tramo.
     */
    private void actualizarEstadoSolicitudSiCorresponde(Integer rutaId) {
        // Buscar la solicitud asociada a esta ruta
        Solicitud solicitud = solicitudRepository.findByRutaId(rutaId);
        
        if (solicitud != null && solicitud.getEstado() == EstadoSolicitudEnum.PROGRAMADA) {
            // Verificar si hay al menos un tramo INICIADO
            List<Tramo> tramos = tramoRepository.findAll().stream()
                    .filter(t -> t.getRuta().getId().equals(rutaId))
                    .toList();
            
            boolean hayTramoIniciado = tramos.stream()
                    .anyMatch(t -> t.getEstado() == EstadoTramoEnum.INICIADO);
            
            if (hayTramoIniciado) {
                solicitud.setEstado(EstadoSolicitudEnum.EN_TRANSITO);
                solicitudRepository.save(solicitud);
            }
        }
    }

    /**
     * Verifica si todos los tramos de una ruta están finalizados.
     * Si es así, marca la solicitud como ENTREGADA.
     */
    private void verificarYMarcarSolicitudEntregada(Integer rutaId) {
        // Buscar la solicitud asociada a esta ruta
        Solicitud solicitud = solicitudRepository.findByRutaId(rutaId);
        
        if (solicitud != null && solicitud.getEstado() == EstadoSolicitudEnum.EN_TRANSITO) {
            // Obtener todos los tramos de la ruta
            List<Tramo> tramos = tramoRepository.findAll().stream()
                    .filter(t -> t.getRuta().getId().equals(rutaId))
                    .toList();
            
            // Verificar si TODOS los tramos están FINALIZADOS
            boolean todosFinalizados = !tramos.isEmpty() && tramos.stream()
                    .allMatch(t -> t.getEstado() == EstadoTramoEnum.FINALIZADO);
            
            if (todosFinalizados) {
                solicitud.setEstado(EstadoSolicitudEnum.ENTREGADA);
                solicitudRepository.save(solicitud);
            }
        }
    }
}
