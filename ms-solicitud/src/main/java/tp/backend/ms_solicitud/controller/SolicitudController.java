package tp.backend.ms_solicitud.controller;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

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
import tp.backend.ms_solicitud.client.ContenedorClientService;
import tp.backend.ms_solicitud.dto.ContenedorDTO;
import tp.backend.ms_solicitud.dto.EstadoResponseDTO;
import tp.backend.ms_solicitud.dto.RutaTentativaDTO;
import tp.backend.ms_solicitud.dto.RutasTentativasResponseDTO;
import tp.backend.ms_solicitud.dto.SolicitudRequest;
import tp.backend.ms_solicitud.entity.Cliente;
import tp.backend.ms_solicitud.entity.EstadoSolicitudEnum;
import tp.backend.ms_solicitud.entity.Ruta;
import tp.backend.ms_solicitud.entity.Solicitud;
import tp.backend.ms_solicitud.entity.Tramo;
import tp.backend.ms_solicitud.service.ClienteService;
import tp.backend.ms_solicitud.service.RutaService;
import tp.backend.ms_solicitud.service.RutaTentativaService;
import tp.backend.ms_solicitud.service.SolicitudService;
import tp.backend.ms_solicitud.service.TramoService;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final RutaService rutaService;
    private final TramoService tramoService;
    private final ClienteService clienteService;
    private final RutaTentativaService rutaTentativaService;
    private final ContenedorClientService contenedorClientService;
    private static final Logger log = LoggerFactory.getLogger(SolicitudController.class);

    @GetMapping
    public ResponseEntity<?> getAll() {
        log.info("GET /api/solicitudes - getAll");
        try {
            List<Solicitud> res = solicitudService.findAll();
            log.debug("GET /api/solicitudes - found {} solicitudes", res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/solicitudes - error al listar solicitudes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener la lista de solicitudes: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        log.info("GET /api/solicitudes/{} - getById", id);
        try {
            Solicitud s = solicitudService.findById(id);
            log.debug("GET /api/solicitudes/{} - result={}", id, s);
            return ResponseEntity.ok(s);
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico del service
            log.warn("GET /api/solicitudes/{} - {}: {}", id, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("GET /api/solicitudes/{} - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar solicitud con ID " + id + ": " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SolicitudRequest request) {
        log.info("POST /api/solicitudes - create");
        try {
            
            if (request == null) {
                log.warn("POST /api/solicitudes - request vacío");
                return ResponseEntity.badRequest().body("La solicitud no puede estar vacía");//error 400
            }

            
            if (request.getOrigen() == null || request.getOrigen().trim().isEmpty()) {
                log.warn("POST /api/solicitudes - origen faltante");
                return ResponseEntity.badRequest().body("El origen es obligatorio");
            }
            if (request.getDestino() == null || request.getDestino().trim().isEmpty()) {
                log.warn("POST /api/solicitudes - destino faltante");
                return ResponseEntity.badRequest().body("El destino es obligatorio");
            }

            if (request.getCliente() == null || request.getCliente().getDniCliente() == null) {
                log.warn("POST /api/solicitudes - DNI del cliente es obligatorio");
                return ResponseEntity.badRequest().body("El DNI del cliente es obligatorio");
            }

            // Validar contenedor
            if (request.getContenedor() == null) {
                log.warn("POST /api/solicitudes - datos del contenedor faltantes");
                return ResponseEntity.badRequest().body("Los datos del contenedor son obligatorios");
            }
            if (request.getContenedor().getPeso() == null || request.getContenedor().getPeso() <= 0) {
                log.warn("POST /api/solicitudes - peso del contenedor inválido");
                return ResponseEntity.badRequest().body("El peso del contenedor debe ser un valor positivo");
            }
            if (request.getContenedor().getVolumen() == null || request.getContenedor().getVolumen() <= 0) {
                log.warn("POST /api/solicitudes - volumen del contenedor inválido");
                return ResponseEntity.badRequest().body("El volumen del contenedor debe ser un valor positivo");
            }

            // 1. Crear o verificar cliente
            Cliente c = request.getCliente();
            Integer clienteId = c.getDniCliente();

            // Verificar si el cliente ya existe, si no, crearlo
            if (!clienteService.existsById(clienteId)) {
                log.info("POST /api/solicitudes - creando nuevo cliente con DNI {}", clienteId);
                clienteService.create(c);
            } else {
                log.debug("POST /api/solicitudes - cliente {} ya existe, se reutiliza", clienteId);
            }

            // 2. Crear contenedor
            ContenedorDTO contenedorDTO = ContenedorDTO.builder()
                .peso(java.math.BigDecimal.valueOf(request.getContenedor().getPeso()))
                .volumen(java.math.BigDecimal.valueOf(request.getContenedor().getVolumen()))
                .build();
            
            log.info("POST /api/solicitudes - creando contenedor con peso={}, volumen={}", 
                     contenedorDTO.getPeso(), contenedorDTO.getVolumen());
            ContenedorDTO contenedorCreado = contenedorClientService.crearContenedor(contenedorDTO);
            Integer contenedorId = contenedorCreado.getIdContenedor().intValue();
            log.info("POST /api/solicitudes - contenedor creado con id={}", contenedorId);

            // 3. Crear solicitud
            Solicitud solicitud = new Solicitud();
            solicitud.setId(null);
            solicitud.setContenedorId(contenedorId);
            solicitud.setClienteId(clienteId);
            // No es necesario setear el estado, ya tiene BORRADOR por defecto
            solicitud.setOrigen(request.getOrigen());
            solicitud.setDestino(request.getDestino());

            log.info("POST /api/solicitudes - creando solicitud para clienteId={}, contenedorId={}", clienteId, contenedorId);
            Solicitud nueva = solicitudService.create(solicitud);
            log.info("POST /api/solicitudes - solicitud creada exitosamente con id={}", nueva.getId());
            return ResponseEntity.created(URI.create("/api/solicitudes/" + nueva.getId())).body(nueva);
            
        } catch (IllegalArgumentException e) {
            log.error("POST /api/solicitudes - error de validación: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("POST /api/solicitudes - error inesperado al crear solicitud: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear la solicitud: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Solicitud solicitud) {
        log.info("PUT /api/solicitudes/{} - update", id);
        try {
            // Validar que el body no sea null
            if (solicitud == null) {
                log.warn("PUT /api/solicitudes/{} - body vacío", id);
                return ResponseEntity.badRequest().body("El cuerpo de la solicitud no puede estar vacío");
            }
            
            Solicitud actualizada = solicitudService.update(id, solicitud);
            log.info("PUT /api/solicitudes/{} - updated successfully", id);
            return ResponseEntity.ok(actualizada);
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico del service (ej: 404 si no existe)
            log.warn("PUT /api/solicitudes/{} - {}: {}", id, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (IllegalArgumentException e) {
            log.error("PUT /api/solicitudes/{} - error de validación: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("PUT /api/solicitudes/{} - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar solicitud con ID " + id + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        log.info("DELETE /api/solicitudes/{} - delete", id);
        try {
            solicitudService.delete(id);
            log.info("DELETE /api/solicitudes/{} - deleted successfully", id);
            return ResponseEntity.ok("Solicitud " + id + " eliminada correctamente");
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico del service (ej: 404 si no existe)
            log.warn("DELETE /api/solicitudes/{} - {}: {}", id, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("DELETE /api/solicitudes/{} - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al eliminar solicitud con ID " + id + ": " + e.getMessage());
        }
    }

    @GetMapping("/by-cliente/{clienteId}")
    public ResponseEntity<?> getByCliente(@PathVariable Integer clienteId) {
        log.info("GET /api/solicitudes/by-cliente/{} - getByCliente", clienteId);
        try {
            if (clienteId == null || clienteId <= 0) {
                log.warn("GET /api/solicitudes/by-cliente - clienteId inválido: {}", clienteId);
                return ResponseEntity.badRequest().body("El ID del cliente debe ser un número positivo");
            }
            
            List<Solicitud> res = solicitudService.findByCliente(clienteId);
            log.debug("GET /api/solicitudes/by-cliente/{} - found {} solicitudes", clienteId, res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/solicitudes/by-cliente/{} - error al buscar solicitudes: {}", clienteId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar solicitudes del cliente " + clienteId + ": " + e.getMessage());
        }
    }

    @GetMapping("/estado/{estadoEnum}")
    public ResponseEntity<?> getByEstado(@PathVariable String estadoEnum) {
        log.info("GET /api/solicitudes/estado/{} - getByEstado", estadoEnum);
        try {
            EstadoSolicitudEnum estado;
            try {
                estado = EstadoSolicitudEnum.valueOf(estadoEnum.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("GET /api/solicitudes/estado - estado inválido: {}", estadoEnum);
                return ResponseEntity.badRequest()
                    .body("Estado inválido. Valores permitidos: BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA");
            }
            
            List<Solicitud> res = solicitudService.findByEstado(estado);
            log.debug("GET /api/solicitudes/estado/{} - found {} solicitudes", estadoEnum, res.size());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("GET /api/solicitudes/estado/{} - error al buscar solicitudes: {}", estadoEnum, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar solicitudes con estado " + estadoEnum + ": " + e.getMessage());
        }
    }

    // GET /api/solicitudes/{id}/estado -> devuelve el estado de la solicitud
    @GetMapping("/{id}/estado")
    public ResponseEntity<?> getEstado(@PathVariable Integer id) {
        log.info("GET /api/solicitudes/{}/estado - getEstado", id);
        try {
            Solicitud s = solicitudService.findById(id);
            
            if (s.getEstado() == null) {
                log.warn("GET /api/solicitudes/{}/estado - solicitud sin estado asignado", id);
                return ResponseEntity.badRequest().body("La solicitud no tiene estado asignado");
            }
            
            EstadoResponseDTO response = new EstadoResponseDTO(s.getEstado().ordinal() + 1, s.getEstado().name());
            log.debug("GET /api/solicitudes/{}/estado - estado={}", id, s.getEstado());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico (404 solicitud o estado no encontrado)
            log.warn("GET /api/solicitudes/{}/estado - {}: {}", id, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("GET /api/solicitudes/{}/estado - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener estado de solicitud " + id + ": " + e.getMessage());
        }
    }

    // GET /api/solicitudes/{id}/ruta -> devuelve la ruta asignada a la solicitud
    @GetMapping("/{id}/ruta")
    public ResponseEntity<?> getRuta(@PathVariable Integer id) {
        log.info("GET /api/solicitudes/{}/ruta - getRuta", id);
        try {
            // Verificar que la solicitud existe primero
            solicitudService.findById(id);
            
            Ruta ruta = rutaService.findBySolicitud(id);
            if (ruta == null) {
                log.debug("GET /api/solicitudes/{}/ruta - no tiene ruta asignada", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("La solicitud no tiene una ruta asignada");
            }
            log.debug("GET /api/solicitudes/{}/ruta - found ruta id={}", id, ruta.getId());
            return ResponseEntity.ok(ruta);
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico (404 si solicitud no existe)
            log.warn("GET /api/solicitudes/{}/ruta - {}: {}", id, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("GET /api/solicitudes/{}/ruta - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al buscar ruta de solicitud " + id + ": " + e.getMessage());
        }
    }

    // PUT /api/solicitudes/{solicitudId}/rutas/{rutaId}/tramos
    // Recibe un listado de tramos (con id) y actualiza cada uno asignándole la ruta indicada
    @PutMapping("/{solicitudId}/rutas/{rutaId}/tramos")
    public ResponseEntity<?> updateTramosForRuta(@PathVariable Integer solicitudId,
                                                           @PathVariable Integer rutaId,
                                                           @RequestBody List<Tramo> tramos) {
        log.info("PUT /api/solicitudes/{}/rutas/{}/tramos - updateTramos count={}", solicitudId, rutaId, tramos != null ? tramos.size() : 0);
        try {
            // Validaciones
            if (tramos == null || tramos.isEmpty()) {
                log.warn("PUT /api/solicitudes/{}/rutas/{}/tramos - lista de tramos vacía", solicitudId, rutaId);
                return ResponseEntity.badRequest().body("La lista de tramos no puede estar vacía");
            }
            
            // Verificar que la solicitud existe
            solicitudService.findById(solicitudId);
            log.debug("PUT /api/solicitudes/{}/rutas/{}/tramos - solicitud encontrada", solicitudId, rutaId);

            Ruta rutaRef = new Ruta();
            rutaRef.setId(rutaId);

            List<Tramo> updated = new java.util.ArrayList<>();
            for (Tramo t : tramos) {
                if (t.getId() == null) {
                    log.warn("PUT /api/solicitudes/{}/rutas/{}/tramos - tramo sin ID encontrado", solicitudId, rutaId);
                    return ResponseEntity.badRequest().body("Todos los tramos deben tener un ID válido");
                }
                // setear la referencia a la ruta
                t.setRuta(rutaRef);
                Tramo u = tramoService.update(t.getId(), t);
                updated.add(u);
            }
            log.info("PUT /api/solicitudes/{}/rutas/{}/tramos - actualizados {} tramos exitosamente", solicitudId, rutaId, updated.size());
            return ResponseEntity.ok(updated);
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico (404 si solicitud/ruta/tramo no existe)
            log.warn("PUT /api/solicitudes/{}/rutas/{}/tramos - {}: {}", solicitudId, rutaId, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("PUT /api/solicitudes/{}/rutas/{}/tramos - error inesperado: {}", solicitudId, rutaId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar tramos de la ruta " + rutaId + ": " + e.getMessage());
        }
    }

    // PUT /api/solicitudes/{solicitudId}/ruta/{rutaId}/tramo/{tramoId}/camion
    // Asigna un camión (por dominio) al tramo. Body: { "dominio": "ABC123" }
    @PutMapping("/{solicitudId}/ruta/{rutaId}/tramo/{tramoId}/camion")
    public ResponseEntity<?> assignCamionToTramo(@PathVariable Integer solicitudId,
                                                    @PathVariable Integer rutaId,
                                                    @PathVariable Integer tramoId,
                                                    @RequestBody java.util.Map<String, String> body) {
        log.info("PUT /api/solicitudes/{}/ruta/{}/tramo/{}/camion - assignCamionToTramo dominio={}", 
                 solicitudId, rutaId, tramoId, body != null ? body.get("dominio") : "null");
        try {
            // Validar que la solicitud existe
            solicitudService.findById(solicitudId);

            String dominio = body != null ? body.get("dominio") : null;
            if (dominio == null || dominio.isBlank()) {
                log.warn("PUT /api/solicitudes/{}/ruta/{}/tramo/{}/camion - dominio faltante o vacío", solicitudId, rutaId, tramoId);
                return ResponseEntity.badRequest().body("El dominio del camión es obligatorio");
            }

            // Usar el método asignarCamion que también actualiza el estado a ASIGNADO
            Tramo updated = tramoService.asignarCamion(tramoId, dominio);
            log.info("PUT /api/solicitudes/{}/ruta/{}/tramo/{}/camion - camión {} asignado exitosamente", solicitudId, rutaId, tramoId, dominio);
            return ResponseEntity.ok(updated);
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico (404 si solicitud/ruta/tramo no existe)
            log.warn("PUT /api/solicitudes/z|{}/ruta/{}/tramo/{}/camion - {}: {}", solicitudId, rutaId, tramoId, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("PUT /api/solicitudes/{}/ruta/{}/tramo/{}/camion - error inesperado: {}", solicitudId, rutaId, tramoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al asignar camión al tramo " + tramoId + ": " + e.getMessage());
        }
    }

    /**
     * Calcula todas las rutas tentativas para una solicitud (sin persistir).
     * Devuelve JSON con opciones de rutas (directa, con depósitos intermedios).
     * Obtiene origen y destino directamente de la solicitud.
     * 
     * GET /api/solicitudes/{id}/rutas-tentativas
     */
    @GetMapping("/{id}/rutas-tentativas")
    public ResponseEntity<?> calcularRutasTentativas(@PathVariable Integer id) {
        log.info("GET /api/solicitudes/{}/rutas-tentativas", id);
        try {
            // Obtener la solicitud con origen y destino
            Solicitud solicitud = solicitudService.findById(id);
            String origen = solicitud.getOrigen();
            String destino = solicitud.getDestino();
            
            log.info("GET /api/solicitudes/{}/rutas-tentativas - origen={} destino={}", id, origen, destino);
            
            RutasTentativasResponseDTO response = rutaTentativaService.calcularRutasTentativas(id, origen, destino);
            log.info("GET /api/solicitudes/{}/rutas-tentativas - calculadas {} opciones de ruta", id, 
                     response != null && response.getRutas() != null ? response.getRutas().size() : 0);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico (404 si solicitud no existe)
            log.warn("GET /api/solicitudes/{}/rutas-tentativas - {}: {}", id, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            log.error("GET /api/solicitudes/{}/rutas-tentativas - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al calcular rutas tentativas para solicitud " + id + ": " + e.getMessage());
        }
    }

    /**
     * Asigna una ruta tentativa seleccionada a la solicitud (persiste en BD).
     * El operador elige una de las opciones calculadas y la envía en el body.
     * 
     * POST /api/solicitudes/{id}/asignar-ruta
     * Body: RutaTentativaDTO (la opción elegida)
     */
    @PostMapping("/{id}/asignar-ruta")
    public ResponseEntity<?> asignarRuta(//el ? es cualquier tipo de dato
            @PathVariable Integer id,
            @RequestBody RutaTentativaDTO rutaSeleccionada) {
        log.info("POST /api/solicitudes/{}/asignar-ruta - asignarRuta opcion={}", id, 
                 rutaSeleccionada != null ? rutaSeleccionada.getNumeroOpcion() : "null");
        try {
            if (rutaSeleccionada == null) {
                log.warn("POST /api/solicitudes/{}/asignar-ruta - ruta seleccionada es null", id);
                return ResponseEntity.badRequest().body("Debe proporcionar una ruta válida");
            }
            
            // Verificar que la solicitud existe
            solicitudService.findById(id);
            
            Ruta rutaPersistida = solicitudService.asignarRuta(id, rutaSeleccionada);
            log.info("POST /api/solicitudes/{}/asignar-ruta - ruta asignada exitosamente con id={}", id, rutaPersistida.getId());
            
            // Devolver la solicitud actualizada con la ruta asignada
            Solicitud solicitudActualizada = solicitudService.findById(id);
            return ResponseEntity.ok(solicitudActualizada);
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico (404 si solicitud no existe)
            log.warn("POST /api/solicitudes/{}/asignar-ruta - {}: {}", id, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (IllegalArgumentException e) {
            log.error("POST /api/solicitudes/{}/asignar-ruta - error de validación: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error al asignar ruta: " + e.getMessage());
        } catch (Exception e) {
            log.error("POST /api/solicitudes/{}/asignar-ruta - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al asignar ruta a solicitud " + id + ": " + e.getMessage());
        }
    }

    /**
     * Calcula y persiste el costo total de la solicitud (invoca la lógica del service).
     * Devuelve la solicitud completa con todos sus datos, rutas, tramos y costos actualizados.
     * POST /api/solicitudes/{id}/total
     */
    @PostMapping("/{id}/total")
    public ResponseEntity<?> calcularCostoTotal(@PathVariable Integer id) {
        log.info("POST /api/solicitudes/{}/total - iniciando cálculo de costo total", id);
        try {
            // Verificar que la solicitud existe primero
            solicitudService.findById(id);
            
            solicitudService.calcularCostoReal(id);
            Solicitud solicitudActualizada = solicitudService.findById(id);
            log.info("POST /api/solicitudes/{}/total - costo total calculado: ${}", 
                     id, solicitudActualizada.getCostoFinal());
            return ResponseEntity.ok(solicitudActualizada);
        } catch (ResponseStatusException e) {
            // Excepciones con código HTTP específico (404 si solicitud no existe)
            log.warn("POST /api/solicitudes/{}/total - {}: {}", id, e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (IllegalStateException e) {
            log.error("POST /api/solicitudes/{}/total - error de estado: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("No se puede calcular el costo: " + e.getMessage());
        } catch (Exception e) {
            log.error("POST /api/solicitudes/{}/total - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al calcular costo real de solicitud " + id + ": " + e.getMessage());
        }
    }

    /**
     * Obtiene los tramos de una solicitud.
     * GET /api/solicitudes/{id}/tramos
     */
    @GetMapping("/{id}/tramos")
    public ResponseEntity<?> obtenerTramos(@PathVariable Integer id) {
        log.info("GET /api/solicitudes/{}/tramos - obteniendo tramos", id);
        try {
            Solicitud solicitud = solicitudService.findById(id);
            
            if (solicitud.getRuta() == null) {
                log.warn("GET /api/solicitudes/{}/tramos - solicitud sin ruta asignada", id);
                return ResponseEntity.badRequest().body("La solicitud no tiene ruta asignada");
            }
            
            List<Tramo> tramos = tramoService.findByRutaId(solicitud.getRuta().getId());
            log.info("GET /api/solicitudes/{}/tramos - encontrados {} tramos", id, tramos.size());
            return ResponseEntity.ok(tramos);
            
        } catch (NoSuchElementException e) {
            log.error("GET /api/solicitudes/{}/tramos - no encontrada: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Solicitud no encontrada con id: " + id);
        } catch (Exception e) {
            log.error("GET /api/solicitudes/{}/tramos - error inesperado: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener tramos de solicitud " + id + ": " + e.getMessage());
        }
    }
}


