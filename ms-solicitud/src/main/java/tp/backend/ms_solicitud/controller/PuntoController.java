package tp.backend.ms_solicitud.controller;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tp.backend.ms_solicitud.entity.Punto;
import tp.backend.ms_solicitud.service.PuntoService;

@RestController
@RequestMapping("/api/puntos")
@RequiredArgsConstructor
public class PuntoController {

    private final PuntoService puntoService;
    private static final Logger log = LoggerFactory.getLogger(PuntoController.class);

    @GetMapping
    public List<Punto> list() {
        log.info("GET /api/puntos - list");
        List<Punto> res = puntoService.findAll();
        log.debug("GET /api/puntos - found {} puntos", res.size());
        return res;
    }

    @GetMapping("/{id}")
    public Punto get(@PathVariable Integer id) {
        log.info("GET /api/puntos/{} - get", id);
        Punto res = puntoService.findById(id);
        log.debug("GET /api/puntos/{} - result={}", id, res);
        return res;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Punto create(@RequestBody Punto punto) {
        log.info("POST /api/puntos - create");
        Punto res = puntoService.create(punto);
        log.info("POST /api/puntos - created id={}", res.getId());
        return res;
    }

    @PutMapping("/{id}")
    public Punto update(@PathVariable Integer id, @RequestBody Punto punto) {
        log.info("PUT /api/puntos/{} - update", id);
        Punto res = puntoService.update(id, punto);
        log.info("PUT /api/puntos/{} - updated", id);
        return res;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        log.info("DELETE /api/puntos/{} - delete", id);
        puntoService.delete(id);
        log.info("DELETE /api/puntos/{} - deleted", id);
        return ResponseEntity.ok("Punto " + id + " eliminado correctamente");
    }
}