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
import tp.backend.ms_contenedor.entity.Contenedor;
import tp.backend.ms_contenedor.service.ContenedorService;

@RestController
@RequestMapping("/api/contenedores") // Ruta base para el controlador
@RequiredArgsConstructor
public class ContenedorController { 

  private final ContenedorService service; // Inyección del servicio, asociamos el service al controller
  private static final Logger log = LoggerFactory.getLogger(ContenedorController.class);

  @PostMapping //endpoint para crear un contenedor
  public ResponseEntity<?> crear(@RequestBody Contenedor contenedor) { //el response entity nos permite devolver el codigo http adecuado
    log.info("POST /api/contenedores - crear");
    try {
      if (contenedor == null) {
        log.warn("POST /api/contenedores - contenedor vacío");
        return ResponseEntity.badRequest().body("El contenedor no puede estar vacío");
      }
      
      Contenedor nuevo = service.crear(contenedor); //la respuesta es un objeto del contenedor que adems tiene las configuraciones de http
      log.info("POST /api/contenedores - created id={}", nuevo.getIdContenedor());
      return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    } catch (IllegalArgumentException e) {
      log.warn("POST /api/contenedores - error de validación: {}", e.getMessage());
      return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
    } catch (Exception e) {
      log.error("POST /api/contenedores - error inesperado: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error al crear contenedor: " + e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> obtener(@PathVariable Integer id) {
    log.info("GET /api/contenedores/{} - obtener", id);
    try {
      if (id == null || id <= 0) {
        log.warn("GET /api/contenedores - ID inválido: {}", id);
        return ResponseEntity.badRequest().body("El ID debe ser un número positivo válido");
      }
      
      Contenedor c = service.obtener(id);
      if (c == null) {
        log.warn("GET /api/contenedores/{} - contenedor no encontrado", id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenedor con ID " + id + " no encontrado");
      }
      log.debug("GET /api/contenedores/{} - result={}", id, c);
      return ResponseEntity.ok(c);
    } catch (Exception e) {
      log.error("GET /api/contenedores/{} - error inesperado: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error al buscar contenedor con ID " + id + ": " + e.getMessage());
    }
  }

  @GetMapping
  public ResponseEntity<?> listar() {
    log.info("GET /api/contenedores - listar");
    try {
      List<Contenedor> res = service.listar();
      log.debug("GET /api/contenedores - found {} contenedores", res.size());
      return ResponseEntity.ok(res);
    } catch (Exception e) {
      log.error("GET /api/contenedores - error al listar: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error al obtener lista de contenedores: " + e.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Contenedor contenedor) {
    log.info("PUT /api/contenedores/{} - actualizar", id);
    try {
      if (id == null || id <= 0) {
        log.warn("PUT /api/contenedores - ID inválido: {}", id);
        return ResponseEntity.badRequest().body("El ID debe ser un número positivo válido");
      }
      if (contenedor == null) {
        log.warn("PUT /api/contenedores/{} - contenedor vacío", id);
        return ResponseEntity.badRequest().body("El contenedor no puede estar vacío");
      }
      
      Contenedor existente = service.obtener(id);
      if (existente == null) {
        log.warn("PUT /api/contenedores/{} - contenedor no encontrado", id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenedor con ID " + id + " no encontrado");
      }
      
      existente.setPeso(contenedor.getPeso());
      existente.setVolumen(contenedor.getVolumen());
      Contenedor actualizado = service.crear(existente);
      log.info("PUT /api/contenedores/{} - updated", id);
      return ResponseEntity.ok(actualizado);
    } catch (IllegalArgumentException e) {
      log.warn("PUT /api/contenedores/{} - error de validación: {}", id, e.getMessage());
      return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
    } catch (Exception e) {
      log.error("PUT /api/contenedores/{} - error inesperado: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error al actualizar contenedor con ID " + id + ": " + e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> eliminar(@PathVariable Integer id) {
    log.info("DELETE /api/contenedores/{} - eliminar", id);
    try {
      if (id == null || id <= 0) {
        log.warn("DELETE /api/contenedores - ID inválido: {}", id);
        return ResponseEntity.badRequest().body("El ID debe ser un número positivo válido");
      }
      
      Contenedor existente = service.obtener(id);
      if (existente == null) {
        log.warn("DELETE /api/contenedores/{} - contenedor no encontrado", id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contenedor con ID " + id + " no encontrado");
      }
      
      service.eliminar(id);
      log.info("DELETE /api/contenedores/{} - deleted", id);
      return ResponseEntity.ok("Contenedor " + id + " eliminado correctamente");
    } catch (Exception e) {
      log.error("DELETE /api/contenedores/{} - error inesperado: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error al eliminar contenedor con ID " + id + ": " + e.getMessage());
    }
  }

}
