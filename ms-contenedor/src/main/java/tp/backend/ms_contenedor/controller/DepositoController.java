package tp.backend.ms_contenedor.controller;

import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tp.backend.ms_contenedor.entity.Deposito;
import tp.backend.ms_contenedor.service.DepositoService;

/**
 * Controller REST para gestionar Depósitos.
 * Expone endpoints CRUD para la entidad Deposito.
 */
@RestController
@RequestMapping("/api/depositos")
@RequiredArgsConstructor
public class DepositoController {

    private final DepositoService service;
    private static final Logger log = LoggerFactory.getLogger(DepositoController.class);

    /**
     * Crea un nuevo depósito
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Deposito deposito) {
        log.info("POST /api/depositos - crear");
        try {
            if (deposito == null) {
                log.warn("POST /api/depositos - depósito vacío");
                return ResponseEntity.badRequest().body("El depósito no puede estar vacío");
            }
            
            Deposito nuevo = service.crear(deposito);
            log.info("POST /api/depositos - created id={}", nuevo.getIdDeposito());
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            log.warn("POST /api/depositos - error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("POST /api/depositos - error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear depósito: " + e.getMessage());
        }
    }

    /**
     * Obtiene un depósito por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        log.info("GET /api/depositos/{} - obtener", id);
        try {
            if (id == null || id <= 0) {
                log.warn("GET /api/depositos - ID inválido: {}", id);
                return ResponseEntity.badRequest().body("El ID debe ser un número positivo válido");
            }
            
            Deposito deposito = service.obtener(id);
            if (deposito == null) {
                log.warn("GET /api/depositos/{} - depósito no encontrado", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Depósito con ID " + id + " no encontrado");
            }
            log.debug("GET /api/depositos/{} - result={}", id, deposito);
            return ResponseEntity.ok(deposito);
        } catch (Exception e) {
            log.error("GET /api/depositos/{} - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar depósito con ID " + id + ": " + e.getMessage());
        }
    }

    /**
     * Lista todos los depósitos disponibles
     */
    @GetMapping
    public ResponseEntity<?> listar() {
        log.info("GET /api/depositos - listar");
        try {
            List<Deposito> res = service.listar();
            log.debug("GET /api/depositos - found {} depositos", res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/depositos - error al listar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener lista de depósitos: " + e.getMessage());
        }
    }

    /**
     * Actualiza un depósito existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Deposito deposito) {
        log.info("PUT /api/depositos/{} - actualizar", id);
        try {
            if (id == null || id <= 0) {
                log.warn("PUT /api/depositos - ID inválido: {}", id);
                return ResponseEntity.badRequest().body("El ID debe ser un número positivo válido");
            }
            if (deposito == null) {
                log.warn("PUT /api/depositos/{} - depósito vacío", id);
                return ResponseEntity.badRequest().body("El depósito no puede estar vacío");
            }
            
            Deposito existente = service.obtener(id);
            if (existente == null) {
                log.warn("PUT /api/depositos/{} - depósito no encontrado", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Depósito con ID " + id + " no encontrado");
            }
            
            existente.setNombre(deposito.getNombre());
            existente.setDireccion(deposito.getDireccion());
            existente.setCostoEstadiaDiario(deposito.getCostoEstadiaDiario());
            Deposito actualizado = service.crear(existente);
            log.info("PUT /api/depositos/{} - updated", id);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/depositos/{} - error de validación: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("PUT /api/depositos/{} - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar depósito con ID " + id + ": " + e.getMessage());
        }
    }

    /**
     * Elimina un depósito
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/depositos/{} - eliminar", id);
        try {
            if (id == null || id <= 0) {
                log.warn("DELETE /api/depositos - ID inválido: {}", id);
                return ResponseEntity.badRequest().body("El ID debe ser un número positivo válido");
            }
            
            Deposito existente = service.obtener(id);
            if (existente == null) {
                log.warn("DELETE /api/depositos/{} - depósito no encontrado", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Depósito con ID " + id + " no encontrado");
            }
            
            service.eliminar(id);
            log.info("DELETE /api/depositos/{} - deleted", id);
            return ResponseEntity.ok("Depósito " + id + " eliminado correctamente");
        } catch (Exception e) {
            log.error("DELETE /api/depositos/{} - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar depósito con ID " + id + ": " + e.getMessage());
        }
    }
}
