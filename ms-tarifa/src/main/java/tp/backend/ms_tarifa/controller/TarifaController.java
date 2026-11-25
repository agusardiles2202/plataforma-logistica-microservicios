package tp.backend.ms_tarifa.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tp.backend.ms_tarifa.entity.Tarifa;
import tp.backend.ms_tarifa.service.TarifaService;

import java.util.List;

@RestController
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
public class TarifaController {
    
    private final TarifaService tarifaService;
    private static final Logger log = LoggerFactory.getLogger(TarifaController.class);

    @GetMapping
    public ResponseEntity<?> list() {
        log.info("GET /api/tarifas - list");
        try {
            List<Tarifa> res = tarifaService.findAll();
            log.debug("GET /api/tarifas - found {} tarifas", res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/tarifas - error al listar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener lista de tarifas: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Integer id) {
        log.info("GET /api/tarifas/{} - get", id);
        try {
            if (id == null || id <= 0) {
                log.warn("GET /api/tarifas - ID inválido: {}", id);
                return ResponseEntity.badRequest().body("El ID debe ser un número positivo válido");
            }
            
            Tarifa res = tarifaService.findById(id);
            log.debug("GET /api/tarifas/{} - result={}", id, res);
            return ResponseEntity.ok(res);
        } catch (ResponseStatusException e) {
            log.warn("GET /api/tarifas/{} - tarifa no encontrada", id);
            return ResponseEntity.status(e.getStatusCode()).body("Tarifa con ID " + id + " no encontrada");
        } catch (Exception e) {
            log.error("GET /api/tarifas/{} - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar tarifa con ID " + id + ": " + e.getMessage());
        }
    }

    /**
     * Obtiene la tarifa vigente para la fecha actual.
     * Este endpoint es usado por otros microservicios para calcular costos.
     */
    @GetMapping("/vigente")
    public ResponseEntity<?> getVigente() {
        log.info("GET /api/tarifas/vigente - getVigente");
        try {
            Tarifa res = tarifaService.findTarifaVigente();
            if (res == null) {
                log.warn("GET /api/tarifas/vigente - no hay tarifa vigente");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No hay tarifa vigente disponible");
            }
            log.debug("GET /api/tarifas/vigente - result id={}", res.getIdTarifa());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/tarifas/vigente - error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar tarifa vigente: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Tarifa tarifa) {
        log.info("POST /api/tarifas - create");
        try {
            if (tarifa == null) {
                log.warn("POST /api/tarifas - tarifa vacía");
                return ResponseEntity.badRequest().body("La tarifa no puede estar vacía");
            }
            
            Tarifa res = tarifaService.create(tarifa);
            log.info("POST /api/tarifas - created id={}", res.getIdTarifa());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalArgumentException e) {
            log.warn("POST /api/tarifas - error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("POST /api/tarifas - error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear tarifa: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Tarifa tarifa) {
        log.info("PUT /api/tarifas/{} - update", id);
        try {
            if (id == null || id <= 0) {
                log.warn("PUT /api/tarifas - ID inválido: {}", id);
                return ResponseEntity.badRequest().body("El ID debe ser un número positivo válido");
            }
            if (tarifa == null) {
                log.warn("PUT /api/tarifas/{} - tarifa vacía", id);
                return ResponseEntity.badRequest().body("La tarifa no puede estar vacía");
            }
            
            Tarifa res = tarifaService.update(id, tarifa);
            log.info("PUT /api/tarifas/{} - updated", id);
            return ResponseEntity.ok(res);
        } catch (ResponseStatusException e) {
            log.warn("PUT /api/tarifas/{} - tarifa no encontrada", id);
            return ResponseEntity.status(e.getStatusCode()).body("Tarifa con ID " + id + " no encontrada");
        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/tarifas/{} - error de validación: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("PUT /api/tarifas/{} - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar tarifa con ID " + id + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        log.info("DELETE /api/tarifas/{} - delete", id);
        try {
            if (id == null || id <= 0) {
                log.warn("DELETE /api/tarifas - ID inválido: {}", id);
                return ResponseEntity.badRequest().body("El ID debe ser un número positivo válido");
            }
            
            tarifaService.delete(id);
            log.info("DELETE /api/tarifas/{} - deleted", id);
            return ResponseEntity.ok("Tarifa " + id + " eliminada correctamente");
        } catch (ResponseStatusException e) {
            log.warn("DELETE /api/tarifas/{} - tarifa no encontrada", id);
            return ResponseEntity.status(e.getStatusCode()).body("Tarifa con ID " + id + " no encontrada");
        } catch (Exception e) {
            log.error("DELETE /api/tarifas/{} - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar tarifa con ID " + id + ": " + e.getMessage());
        }
    }
}
