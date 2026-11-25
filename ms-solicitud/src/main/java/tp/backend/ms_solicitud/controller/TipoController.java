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
import tp.backend.ms_solicitud.entity.Tipo;
import tp.backend.ms_solicitud.service.TipoService;

@RestController
@RequestMapping("/api/tipos")
@RequiredArgsConstructor
public class TipoController {

    private final TipoService tipoService;
    private static final Logger log = LoggerFactory.getLogger(TipoController.class);

    @GetMapping
    public List<Tipo> list() {
        log.info("GET /api/tipos - list");
        List<Tipo> res = tipoService.findAll();
        log.debug("GET /api/tipos - found {} tipos", res.size());
        return res;
    }

    @GetMapping("/{id}")
    public Tipo get(@PathVariable Integer id) {
        log.info("GET /api/tipos/{} - get", id);
        Tipo res = tipoService.findById(id);
        log.debug("GET /api/tipos/{} - result={}", id, res);
        return res;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tipo create(@RequestBody Tipo tipo) {
        log.info("POST /api/tipos - create");
        Tipo res = tipoService.create(tipo);
        log.info("POST /api/tipos - created id={}", res.getId());
        return res;
    }

    @PutMapping("/{id}")
    public Tipo update(@PathVariable Integer id, @RequestBody Tipo tipo) {
        log.info("PUT /api/tipos/{} - update", id);
        Tipo res = tipoService.update(id, tipo);
        log.info("PUT /api/tipos/{} - updated", id);
        return res;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        log.info("DELETE /api/tipos/{} - delete", id);
        tipoService.delete(id);
        log.info("DELETE /api/tipos/{} - deleted", id);
        return ResponseEntity.ok("Tipo " + id + " eliminado correctamente");
    }
}
