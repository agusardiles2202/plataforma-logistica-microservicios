package tp.backend.ms_solicitud.geo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/distancia")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;
    private static final Logger log = LoggerFactory.getLogger(GeoController.class);

    @GetMapping
    public DistanciaDTO calcularDistancia(
            @RequestParam String origen,
            @RequestParam String destino
    ) {
        log.info("GET /api/distancia - calcularDistancia origen={} destino={}", origen, destino);
        DistanciaDTO res = geoService.calcularDistancia(origen, destino);
        log.debug("Distancia calculada: {}", res);
        return res;
    }
}
