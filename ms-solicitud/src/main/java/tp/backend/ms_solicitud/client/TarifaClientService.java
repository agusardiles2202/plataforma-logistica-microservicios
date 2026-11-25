package tp.backend.ms_solicitud.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import tp.backend.ms_solicitud.dto.TarifaDTO;

/**
 * Cliente REST para comunicarse con ms-tarifa y obtener tarifas vigentes.
 */
@Slf4j
@Service
public class TarifaClientService {

    private final RestClient restClient;
    private final String tarifaServiceUrl;

    public TarifaClientService(
            RestClient.Builder restClientBuilder,
            @Value("${tarifa.service.url:http://ms-tarifa:8085}") String tarifaServiceUrl) {
        this.tarifaServiceUrl = tarifaServiceUrl;
        this.restClient = restClientBuilder.baseUrl(tarifaServiceUrl).build();
    }

    /**
     * Obtiene la tarifa vigente actual desde ms-tarifa.
     */
    public TarifaDTO obtenerTarifaVigente() {
        log.info("Consultando tarifa vigente en: {}/api/tarifas/vigente", tarifaServiceUrl);

        try {
            TarifaDTO tarifa = restClient.get()
                    .uri("/api/tarifas/vigente")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(TarifaDTO.class);

            if (tarifa == null) {
                log.error("No se encontró tarifa vigente en ms-tarifa");
                throw new RuntimeException("No hay tarifa vigente configurada");
            }

            log.info("Tarifa vigente obtenida: valorLitro={}", tarifa.getValorLitroCombustible());
            return tarifa;

        } catch (Exception e) {
            log.error("Error al obtener tarifa vigente desde ms-tarifa: {}", e.getMessage());
            throw new RuntimeException("Error al consultar tarifa: " + e.getMessage(), e);
        }
    }
}
