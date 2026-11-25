package tp.backend.ms_solicitud.controller;

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
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tp.backend.ms_solicitud.entity.Cliente;
import tp.backend.ms_solicitud.service.ClienteService;


@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    @GetMapping
    public ResponseEntity<?> list() {
        log.info("GET /api/clientes - list");
        try {
            List<Cliente> res = clienteService.findAll();
            log.debug("GET /api/clientes - found {} clientes", res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/clientes - error al listar clientes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener la lista de clientes: " + e.getMessage());
        }
    }

    @GetMapping("/{dni}")
    public ResponseEntity<?> get(@PathVariable Integer dni) {
        log.info("GET /api/clientes/{} - get", dni);
        try {
            if (dni == null || dni <= 0) {
                log.warn("GET /api/clientes - DNI inválido: {}", dni);
                return ResponseEntity.badRequest().body("El DNI debe ser un número positivo válido");
            }
            
            Cliente res = clienteService.findById(dni);
            log.debug("GET /api/clientes/{} - result={}", dni, res);
            return ResponseEntity.ok(res);
        } catch (ResponseStatusException e) {
            log.warn("GET /api/clientes/{} - cliente no encontrado", dni);
            return ResponseEntity.status(e.getStatusCode()).body("Cliente con DNI " + dni + " no encontrado");
        } catch (Exception e) {
            log.error("GET /api/clientes/{} - error inesperado: {}", dni, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar cliente con DNI " + dni + ": " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Cliente cliente) {
        log.info("POST /api/clientes - create");
        try {
            if (cliente == null) {
                log.warn("POST /api/clientes - cliente vacío");
                return ResponseEntity.badRequest().body("El cliente no puede estar vacío");
            }
            if (cliente.getDniCliente() == null || cliente.getDniCliente() <= 0) {
                log.warn("POST /api/clientes - DNI inválido: {}", cliente.getDniCliente());
                return ResponseEntity.badRequest().body("El DNI del cliente es obligatorio y debe ser positivo");
            }
            
            Cliente res = clienteService.create(cliente);
            log.info("POST /api/clientes - created dni={}", res.getDniCliente());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalArgumentException e) {
            log.warn("POST /api/clientes - error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("POST /api/clientes - error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear cliente: " + e.getMessage());
        }
    }

    @PutMapping("/{dni}")
    public ResponseEntity<?> update(@PathVariable Integer dni, @RequestBody Cliente cliente) {
        log.info("PUT /api/clientes/{} - update", dni);
        try {
            if (dni == null || dni <= 0) {
                log.warn("PUT /api/clientes - DNI inválido: {}", dni);
                return ResponseEntity.badRequest().body("El DNI debe ser un número positivo válido");
            }
            if (cliente == null) {
                log.warn("PUT /api/clientes/{} - cliente vacío", dni);
                return ResponseEntity.badRequest().body("El cliente no puede estar vacío");
            }
            
            Cliente res = clienteService.update(dni, cliente);
            log.info("PUT /api/clientes/{} - updated", dni);
            return ResponseEntity.ok(res);
        } catch (ResponseStatusException e) {
            log.warn("PUT /api/clientes/{} - cliente no encontrado", dni);
            return ResponseEntity.status(e.getStatusCode()).body("Cliente con DNI " + dni + " no encontrado");
        } catch (IllegalArgumentException e) {
            log.warn("PUT /api/clientes/{} - error de validación: {}", dni, e.getMessage());
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("PUT /api/clientes/{} - error inesperado: {}", dni, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar cliente con DNI " + dni + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/{dni}")
    public ResponseEntity<String> delete(@PathVariable Integer dni) {
        log.info("DELETE /api/clientes/{} - delete", dni);
        try {
            if (dni == null || dni <= 0) {
                log.warn("DELETE /api/clientes - DNI inválido: {}", dni);
                return ResponseEntity.badRequest().body("El DNI debe ser un número positivo válido");
            }
            
            clienteService.delete(dni);
            log.info("DELETE /api/clientes/{} - deleted", dni);
            return ResponseEntity.ok("Cliente " + dni + " eliminado correctamente");
        } catch (ResponseStatusException e) {
            log.warn("DELETE /api/clientes/{} - cliente no encontrado", dni);
            return ResponseEntity.status(e.getStatusCode()).body("Cliente con DNI " + dni + " no encontrado");
        } catch (Exception e) {
            log.error("DELETE /api/clientes/{} - error inesperado: {}", dni, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar cliente con DNI " + dni + ": " + e.getMessage());
        }
    }
}
