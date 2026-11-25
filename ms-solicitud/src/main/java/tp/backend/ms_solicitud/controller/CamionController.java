package tp.backend.ms_solicitud.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tp.backend.ms_solicitud.dto.CamionContenedorCompatibilidadDTO;
import tp.backend.ms_solicitud.entity.Camion;
import tp.backend.ms_solicitud.service.CamionService;

import java.util.List;

@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
public class CamionController {

    private final CamionService camionService;
    private static final Logger log = LoggerFactory.getLogger(CamionController.class);

    @GetMapping
    public ResponseEntity<?> list() {
        log.info("GET /api/camiones - list");
        try {
            List<Camion> res = camionService.findAll();
            log.debug("GET /api/camiones - found {} camiones", res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/camiones - error al listar camiones: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener la lista de camiones: " + e.getMessage());
        }
    }

    // GET /api/camiones/estado -> devuelve estado (disponibilidad) de cada camion
    @GetMapping("/estado")
    public ResponseEntity<?> estados() {
        log.info("GET /api/camiones/estado - estados");
        try {
            List<Camion> all = camionService.findAll();
            List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (Camion c : all) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("dominio", c.getDominio());
                m.put("disponibilidad", c.getDisponibilidad());
                out.add(m);
            }
            log.debug("GET /api/camiones/estado - returned {} items", out.size());
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            log.error("GET /api/camiones/estado - error al obtener estados: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener estados de camiones: " + e.getMessage());
        }
    }

    @GetMapping("/{dominio}")
    public ResponseEntity<?> get(@PathVariable String dominio) {
        log.info("GET /api/camiones/{} - get", dominio);
        try {
            if (dominio == null || dominio.trim().isEmpty()) {
                log.warn("GET /api/camiones - dominio vacío");
                return ResponseEntity.badRequest().body("El dominio no puede estar vacío");
            }
            
            Camion res = camionService.findById(dominio);
            log.debug("GET /api/camiones/{} - result={}", dominio, res);
            return ResponseEntity.ok(res);
        } catch (ResponseStatusException e) {
            log.warn("GET /api/camiones/{} - camión no encontrado", dominio);
            return ResponseEntity.status(e.getStatusCode()).body("Camión con dominio " + dominio + " no encontrado");
        } catch (Exception e) {
            log.error("GET /api/camiones/{} - error inesperado: {}", dominio, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar camión con dominio " + dominio + ": " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Camion camion) {
        log.info("POST /api/camiones - create");
        try {
            if (camion == null) {
                log.warn("POST /api/camiones - camión vacío");
                return ResponseEntity.badRequest().body("El camión no puede estar vacío");
            }
            if (camion.getDominio() == null || camion.getDominio().trim().isEmpty()) {
                log.warn("POST /api/camiones - dominio vacío");
                return ResponseEntity.badRequest().body("El dominio del camión es obligatorio");
            }
            
            Camion res = camionService.create(camion);
            log.info("POST /api/camiones - created dominio={}", res.getDominio());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalArgumentException e) {
            log.warn("POST /api/camiones - error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("POST /api/camiones - error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear camión: " + e.getMessage());
        }
    }

    @PutMapping("/{dominio}")
    public ResponseEntity<?> update(@PathVariable String dominio, @RequestBody Camion camion) {
        log.info("PUT /api/camiones/{} - update", dominio);
        try {
            if (dominio == null || dominio.trim().isEmpty()) {
                log.warn("PUT /api/camiones - dominio vacío");
                return ResponseEntity.badRequest().body("El dominio no puede estar vacío");
            }
            if (camion == null) {
                log.warn("PUT /api/camiones/{} - camión vacío", dominio);
                return ResponseEntity.badRequest().body("El camión no puede estar vacío");
            }
            
            Camion res = camionService.update(dominio, camion);
            log.info("PUT /api/camiones/{} - updated", dominio);
            return ResponseEntity.ok(res);
        } catch (ResponseStatusException e) {
            log.warn("PUT /api/camiones/{} - camión no encontrado", dominio);
            return ResponseEntity.status(e.getStatusCode()).body("Camión con dominio " + dominio + " no encontrado");
        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/camiones/{} - error de validación: {}", dominio, e.getMessage());
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("PUT /api/camiones/{} - error inesperado: {}", dominio, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar camión con dominio " + dominio + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/{dominio}")
    public ResponseEntity<String> delete(@PathVariable String dominio) {
        log.info("DELETE /api/camiones/{} - delete", dominio);
        try {
            if (dominio == null || dominio.trim().isEmpty()) {
                log.warn("DELETE /api/camiones - dominio vacío");
                return ResponseEntity.badRequest().body("El dominio no puede estar vacío");
            }
            
            camionService.delete(dominio);
            log.info("DELETE /api/camiones/{} - deleted", dominio);
            return ResponseEntity.ok("Camión " + dominio + " eliminado correctamente");
        } catch (ResponseStatusException e) {
            log.warn("DELETE /api/camiones/{} - camión no encontrado", dominio);
            return ResponseEntity.status(e.getStatusCode()).body("Camión con dominio " + dominio + " no encontrado");
        } catch (Exception e) {
            log.error("DELETE /api/camiones/{} - error inesperado: {}", dominio, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar camión con dominio " + dominio + ": " + e.getMessage());
        }
    }

    /**
     * Verifica si un camión específico puede transportar un contenedor.
     * Consulta las dimensiones del contenedor desde ms-contenedor.
     * 
     * @param dominio Dominio/matrícula del camión
     * @param idContenedor ID del contenedor
     * @return Información de compatibilidad con restricciones
     */
    @GetMapping("/{dominio}/puede-transportar/{idContenedor}")
    public CamionContenedorCompatibilidadDTO puedeTransportar(
            @PathVariable String dominio, 
            @PathVariable Integer idContenedor) {
        log.info("GET /api/camiones/{}/puede-transportar/{} - puedeTransportar", dominio, idContenedor);
        CamionContenedorCompatibilidadDTO res = camionService.puedeTransportar(dominio, idContenedor);
        log.debug("GET /api/camiones/{}/puede-transportar/{} - puedeTransportar={}", dominio, idContenedor, res.isPuedeTransportar());
        return res;
    }

    /**
     * Lista todos los camiones compatibles con un contenedor específico.
     * Solo devuelve los camiones que SÍ pueden transportarlo.
     * 
     * @param idContenedor ID del contenedor
     * @return Lista de camiones compatibles con sus restricciones
     */
    @GetMapping("/compatibles-para-contenedor/{idContenedor}")
    public List<CamionContenedorCompatibilidadDTO> obtenerCamionesCompatibles(
            @PathVariable Integer idContenedor) {
        log.info("GET /api/camiones/compatibles-para-contenedor/{} - obtenerCamionesCompatibles", idContenedor);
        List<CamionContenedorCompatibilidadDTO> res = camionService.obtenerCamionesCompatibles(idContenedor);
        log.debug("GET /api/camiones/compatibles-para-contenedor/{} - found {} compatibles", idContenedor, res.size());
        return res;
    }
}
