package tp.backend.ms_solicitud.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import tp.backend.ms_solicitud.dto.DepositoDTO;

import java.util.List;

/**
 * Cliente REST para comunicarse con ms-contenedor y obtener depósitos.
 */
@Slf4j
@Service
public class DepositoClientService {

    private final RestClient restClient;
    private final String contenedorServiceUrl;

    public DepositoClientService(
            RestClient.Builder restClientBuilder,
            @Value("${contenedor.service.url:http://ms-contenedor:8083}") String contenedorServiceUrl) {
        this.contenedorServiceUrl = contenedorServiceUrl;
        this.restClient = restClientBuilder.baseUrl(contenedorServiceUrl).build();
    }

    /**
     * Obtiene todos los depósitos disponibles desde ms-contenedor.
     */
    public List<DepositoDTO> obtenerTodosLosDepositos() {
        log.info("Consultando depósitos en: {}/api/depositos", contenedorServiceUrl);

        try {
            List<DepositoDTO> depositos = restClient.get()
                    .uri("/api/depositos")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<DepositoDTO>>() {});

            if (depositos == null || depositos.isEmpty()) {
                log.warn("No se encontraron depósitos en ms-contenedor");
                return List.of();
            }

            log.info("Se obtuvieron {} depósitos desde ms-contenedor", depositos.size());
            return depositos;

        } catch (Exception e) {
            log.error("Error al obtener depósitos desde ms-contenedor: {}", e.getMessage());
            throw new RuntimeException("Error al consultar depósitos: " + e.getMessage(), e);
        }
    }
}
