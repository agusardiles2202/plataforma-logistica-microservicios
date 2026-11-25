package tp.backend.ms_solicitud.controller;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tp.backend.ms_solicitud.entity.Ruta;
import tp.backend.ms_solicitud.service.RutaService;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
public class RutaController {

    private final RutaService rutaService;
    private static final Logger log = LoggerFactory.getLogger(RutaController.class);

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            log.info("GET /api/rutas - getAll");
            List<Ruta> res = rutaService.findAll();
            log.debug("GET /api/rutas - found {} rutas", res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/rutas - error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener las rutas: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            log.info("GET /api/rutas/{} - getById", id);
            Ruta r = rutaService.findById(id);
            if (r == null) {
                log.warn("GET /api/rutas/{} - not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ruta no encontrada con id: " + id);
            }
            log.debug("GET /api/rutas/{} - result={}", id, r);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            log.error("GET /api/rutas/{} - error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener la ruta: " + e.getMessage());
        }
    }

    @GetMapping("/by-solicitud/{solicitudId}")
    public ResponseEntity<?> getBySolicitud(@PathVariable Integer solicitudId) {
        try {
            log.info("GET /api/rutas/by-solicitud/{} - getBySolicitud", solicitudId);
            Ruta ruta = rutaService.findBySolicitud(solicitudId);
            if (ruta == null) {
                log.debug("GET /api/rutas/by-solicitud/{} - no tiene ruta asignada", solicitudId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("La solicitud no tiene una ruta asignada");
            }
            log.debug("GET /api/rutas/by-solicitud/{} - found ruta id={}", solicitudId, ruta.getId());
            return ResponseEntity.ok(ruta);
        } catch (Exception e) {
            log.error("GET /api/rutas/by-solicitud/{} - error: {}", solicitudId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar la ruta por solicitud: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Ruta ruta) {
        try {
            log.info("POST /api/rutas - create");
            Ruta nueva = rutaService.create(ruta);
            log.info("POST /api/rutas - created id={}", nueva.getId());
            return ResponseEntity.created(URI.create("/api/rutas/" + nueva.getId())).body(nueva);
        } catch (IllegalArgumentException e) {
            log.error("POST /api/rutas - bad request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Datos inválidos: " + e.getMessage());
        } catch (Exception e) {
            log.error("POST /api/rutas - error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear la ruta: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Ruta ruta) {
        try {
            log.info("PUT /api/rutas/{} - update", id);
            Ruta actualizada = rutaService.update(id, ruta);
            if (actualizada == null) {
                log.warn("PUT /api/rutas/{} - not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ruta no encontrada con id: " + id);
            }
            log.info("PUT /api/rutas/{} - updated", id);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            log.error("PUT /api/rutas/{} - bad request: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Datos inválidos: " + e.getMessage());
        } catch (Exception e) {
            log.error("PUT /api/rutas/{} - error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar la ruta: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            log.info("DELETE /api/rutas/{} - delete", id);
            rutaService.delete(id);
            log.info("DELETE /api/rutas/{} - deleted", id);
            return ResponseEntity.ok("Ruta " + id + " eliminada correctamente");
        } catch (IllegalArgumentException e) {
            log.error("DELETE /api/rutas/{} - not found: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Ruta no encontrada con id: " + id);
        } catch (Exception e) {
            log.error("DELETE /api/rutas/{} - error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar la ruta: " + e.getMessage());
        }
    }
}
