package tp.backend.ms_solicitud.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import tp.backend.ms_solicitud.dto.ContenedorDTO;

@Slf4j // Esto es para habilitar el logging con Lombok
@Service
public class ContenedorClientService {

    private final RestClient restClient;
    private final String contenedorServiceUrl;

    public ContenedorClientService(
            RestClient.Builder restClientBuilder,
            @Value("${contenedor.service.url:http://ms-contenedor:8083}") String contenedorServiceUrl) {
        this.contenedorServiceUrl = contenedorServiceUrl;
        this.restClient = restClientBuilder.baseUrl(contenedorServiceUrl).build();
    }

    public ContenedorDTO obtenerContenedor(Integer id) {
        log.info("Consultando contenedor {} en: {}", id, contenedorServiceUrl);

        try {
            ContenedorDTO contenedor = restClient.get()
                    .uri("/api/contenedores/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ContenedorDTO.class);

            if (contenedor == null) {
                log.error("Contenedor {} no encontrado", id);
                throw new RuntimeException("Contenedor no encontrado con ID: " + id);
            }

            log.info("Contenedor encontrado: peso={}, volumen={}", contenedor.getPeso(), contenedor.getVolumen());
            return contenedor;

        } catch (Exception e) {
            log.error("Error al obtener contenedor {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al consultar microservicio ms-contenedor: " + e.getMessage(), e);
        }
    }

    public ContenedorDTO crearContenedor(ContenedorDTO contenedor) {
        log.info("Creando contenedor en: {} - peso={}, volumen={}", 
                 contenedorServiceUrl, contenedor.getPeso(), contenedor.getVolumen());

        try {
            ContenedorDTO creado = restClient.post()
                    .uri("/api/contenedores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(contenedor)
                    .retrieve()
                    .body(ContenedorDTO.class);

            if (creado == null || creado.getIdContenedor() == null) {
                log.error("Error al crear contenedor: respuesta vacía o sin ID");
                throw new RuntimeException("Error al crear contenedor: respuesta inválida del servicio");
            }

            log.info("Contenedor creado exitosamente con ID: {}", creado.getIdContenedor());
            return creado;

        } catch (Exception e) {
            log.error("Error al crear contenedor: {}", e.getMessage());
            throw new RuntimeException("Error al crear contenedor en ms-contenedor: " + e.getMessage(), e);
        }
    }
}
