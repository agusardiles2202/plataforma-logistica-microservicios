package tp.backend.ms_solicitud.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tp.backend.ms_solicitud.entity.Tramo;
import tp.backend.ms_solicitud.service.TramoService;



@RestController
@RequestMapping("/api/tramos")
@RequiredArgsConstructor
public class TramoController {

    private final TramoService tramoService;
    private static final Logger log = LoggerFactory.getLogger(TramoController.class);

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> list() {
        log.info("GET /api/tramos - list");
        try {
            List<Tramo> res = tramoService.findAll();
            log.debug("GET /api/tramos - found {} tramos", res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/tramos - error al listar tramos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener la lista de tramos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> get(@PathVariable Integer id) {
        log.info("GET /api/tramos/{} - get", id);
        try {
            Tramo res = tramoService.findById(id);
            log.debug("GET /api/tramos/{} - result={}", id, res);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/tramos/{} - error al buscar tramo: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar tramo con ID " + id + ": " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Tramo tramo) {
        log.info("POST /api/tramos - create");
        try {
            // Validaciones básicas
            if (tramo == null) {
                log.warn("POST /api/tramos - tramo es null");
                return ResponseEntity.badRequest().body("El tramo no puede estar vacío");
            }
            if (tramo.getRuta() == null || tramo.getRuta().getId() == null) {
                log.warn("POST /api/tramos - ruta faltante o sin ID");
                return ResponseEntity.badRequest().body("La ruta es obligatoria");
            }
            if (tramo.getCamion() == null || tramo.getCamion().getDominio() == null) {
                log.warn("POST /api/tramos - camion faltante o sin dominio");
                return ResponseEntity.badRequest().body("El camión es obligatorio");
            }
            
            Tramo res = tramoService.create(tramo);
            log.info("POST /api/tramos - created id={}", res.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalArgumentException e) {
            log.error("POST /api/tramos - error de validación: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("POST /api/tramos - error al crear tramo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear el tramo: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        log.info("DELETE /api/tramos/{} - delete", id);
        try {
            tramoService.delete(id);
            log.info("DELETE /api/tramos/{} - deleted", id);
            return ResponseEntity.ok("Tramo " + id + " eliminado correctamente");
        } catch (Exception e) {
            log.error("DELETE /api/tramos/{} - error al eliminar tramo: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar tramo con ID " + id + ": " + e.getMessage());
        }
    }

    // PATCH /api/tramos/{id}/inicio  -> registrar inicio de viaje (horaInicioReal)
    @PatchMapping("/{id}/inicio")
    public ResponseEntity<?> registrarInicio(@PathVariable Integer id, @RequestBody InicioRequest body) {
        log.info("PATCH /api/tramos/{}/inicio - registrarInicio horaInicioReal={}", id, body.getHoraInicioReal());
        try {
            // Validaciones
            if (body == null || body.getHoraInicioReal() == null) {
                log.warn("PATCH /api/tramos/{}/inicio - horaInicioReal es null", id);
                return ResponseEntity.badRequest().body("La hora de inicio es obligatoria");
            }
            
            // Validar que la fecha no sea futura
            if (body.getHoraInicioReal().isAfter(java.time.LocalDateTime.now())) {
                log.warn("PATCH /api/tramos/{}/inicio - fecha futura: {}", id, body.getHoraInicioReal());
                return ResponseEntity.badRequest().body("La hora de inicio no puede ser futura");
            }
            
            Tramo res = tramoService.registrarInicio(id, body.getHoraInicioReal());
            log.info("PATCH /api/tramos/{}/inicio - registrado exitosamente", id);
            return ResponseEntity.ok(res);
            
        } catch (IllegalStateException e) {
            log.error("PATCH /api/tramos/{}/inicio - estado inválido: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error de estado: " + e.getMessage());
        } catch (Exception e) {
            log.error("PATCH /api/tramos/{}/inicio - error al registrar inicio: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al registrar inicio del tramo " + id + ": " + e.getMessage());
        }
    }

    // PATCH /api/tramos/{id}/fin -> registrar fin de viaje (horaFinReal)
    @PatchMapping("/{id}/fin")
    public ResponseEntity<?> registrarFin(@PathVariable Integer id, @RequestBody FinRequest body) {
        log.info("PATCH /api/tramos/{}/fin - registrarFin horaFinReal={}", id, body.getHoraFinReal());
        try {
            // Validaciones
            if (body == null || body.getHoraFinReal() == null) {
                log.warn("PATCH /api/tramos/{}/fin - horaFinReal es null", id);
                return ResponseEntity.badRequest().body("La hora de fin es obligatoria");
            }
            
            // Validar que la fecha no sea futura
            if (body.getHoraFinReal().isAfter(java.time.LocalDateTime.now())) {
                log.warn("PATCH /api/tramos/{}/fin - fecha futura: {}", id, body.getHoraFinReal());
                return ResponseEntity.badRequest().body("La hora de fin no puede ser futura");
            }
            
            Tramo res = tramoService.registrarFin(id, body.getHoraFinReal());
            log.info("PATCH /api/tramos/{}/fin - registrado exitosamente", id);
            return ResponseEntity.ok(res);
            
        } catch (IllegalStateException e) {
            log.error("PATCH /api/tramos/{}/fin - estado inválido: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error de estado: " + e.getMessage());
        } catch (Exception e) {
            log.error("PATCH /api/tramos/{}/fin - error al registrar fin: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al registrar fin del tramo " + id + ": " + e.getMessage());
        }
    }

    /**
     * Endpoint para listar tramos asignados a un camión por su dominio (p. ej. ABC123).
     * Usado por transportistas para ver tramos de su camión.
     */
    @GetMapping("/by-camion/{dominio}")
    public ResponseEntity<?> findByCamion(@PathVariable String dominio) {
        log.info("GET /api/tramos/by-camion/{} - findByCamion", dominio);
        try {
            if (dominio == null || dominio.trim().isEmpty()) {
                log.warn("GET /api/tramos/by-camion - dominio vacío");
                return ResponseEntity.badRequest().body("El dominio del camión es obligatorio");
            }
            
            List<Tramo> res = tramoService.findByCamionDominio(dominio);
            log.debug("GET /api/tramos/by-camion/{} - found {} tramos", dominio, res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/tramos/by-camion/{} - error al buscar tramos: {}", dominio, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar tramos del camión " + dominio + ": " + e.getMessage());
        }
    }

    /**
     * Obtiene solo el estado de un tramo.
     * GET /api/tramos/{id}/estado
     */
    @GetMapping("/{id}/estado")
    public ResponseEntity<?> obtenerEstado(@PathVariable Integer id) {
        log.info("GET /api/tramos/{}/estado - consultando estado", id);
        try {
            Tramo tramo = tramoService.findById(id);
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("estado", tramo.getEstado().toString());
            log.info("GET /api/tramos/{}/estado - estado: {}", id, tramo.getEstado());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("GET /api/tramos/{}/estado - error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener estado del tramo " + id + ": " + e.getMessage());
        }
    }

    /**
     * Endpoint para asignar un camión a un tramo.
     * Cambia el estado del tramo a ASIGNADO.
     */
    @PatchMapping("/{id}/asignar-camion")
    public ResponseEntity<?> asignarCamion(@PathVariable Integer id, @RequestBody AsignarCamionRequest body) {
        log.info("PATCH /api/tramos/{}/asignar-camion - dominio={}", id, body.getDominioCamion());
        try {
            if (body == null || body.getDominioCamion() == null || body.getDominioCamion().trim().isEmpty()) {
                log.warn("PATCH /api/tramos/{}/asignar-camion - dominio vacío", id);
                return ResponseEntity.badRequest().body("El dominio del camión es obligatorio");
            }
            
            Tramo res = tramoService.asignarCamion(id, body.getDominioCamion());
            log.info("PATCH /api/tramos/{}/asignar-camion - asignado exitosamente", id);
            return ResponseEntity.ok(res);
            
        } catch (Exception e) {
            log.error("PATCH /api/tramos/{}/asignar-camion - error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al asignar camión al tramo " + id + ": " + e.getMessage());
        }
    }

    // DTOs para los endpoints de inicio/fin/asignar-camion
    public static class InicioRequest {
        private java.time.LocalDateTime horaInicioReal;

        public java.time.LocalDateTime getHoraInicioReal() { return horaInicioReal; }
        public void setHoraInicioReal(java.time.LocalDateTime horaInicioReal) 
        { this.horaInicioReal = horaInicioReal; }
    }

    public static class FinRequest {
        private java.time.LocalDateTime horaFinReal;
     
        public java.time.LocalDateTime getHoraFinReal() { return horaFinReal; }
        public void setHoraFinReal(java.time.LocalDateTime horaFinReal) { this.horaFinReal = horaFinReal; }
    }

    public static class AsignarCamionRequest {
        private String dominioCamion;
        
        public String getDominioCamion() { return dominioCamion; }
        public void setDominioCamion(String dominioCamion) { this.dominioCamion = dominioCamion; }
    }
}
