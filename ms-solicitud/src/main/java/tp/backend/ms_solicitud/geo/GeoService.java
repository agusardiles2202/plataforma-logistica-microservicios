package tp.backend.ms_solicitud.geo;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeoService {

    private static final Logger log = LoggerFactory.getLogger(GeoService.class);
    private final RestClient restClient;
    
    @Value("${google.maps.apikey}")
    private String apiKey;

    public GeoService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public DistanciaDTO calcularDistancia(String origen, String destino) {
        // Construir URL correctamente codificada
        String url = UriComponentsBuilder
            .fromUriString("https://maps.googleapis.com/maps/api/distancematrix/json")
            .queryParam("origins", origen)
            .queryParam("destinations", destino)
            .queryParam("key", apiKey)
            .build()
            .toUriString();

        log.info("URL de Google Maps: {}", url);

        String respuesta = restClient.get()
            .uri(url)
            .retrieve()
            .body(String.class);

        log.info("Respuesta de Google Maps: {}", respuesta);
        return parsearRespuesta(respuesta, origen, destino);
    }

    /**
     * Geocodifica una dirección usando Google Geocoding API
     * @param direccion Dirección textual (ej: "Córdoba, Argentina")
     * @return CoordenadasDTO con lat/lng o null si falla
     */
    public CoordenadasDTO geocodificar(String direccion) {
        try {
            String url = UriComponentsBuilder
                .fromUriString("https://maps.googleapis.com/maps/api/geocode/json")
                .queryParam("address", direccion)
                .queryParam("key", apiKey)
                .build()
                .toUriString();

            log.info("Geocodificando dirección: {}", direccion);

            String respuesta = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

            return parsearRespuestaGeocoding(respuesta, direccion);
        } catch (Exception e) {
            log.error("Error al geocodificar dirección '{}': {}", direccion, e.getMessage());
            return new CoordenadasDTO(null, null, direccion);
        }
    }

    private CoordenadasDTO parsearRespuestaGeocoding(String json, String direccionOriginal) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            
            String status = root.path("status").asText();
            if (!"OK".equals(status)) {
                log.warn("Geocoding falló con status: {} para dirección: {}", status, direccionOriginal);
                return new CoordenadasDTO(null, null, direccionOriginal);
            }

            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);
                JsonNode location = firstResult.path("geometry").path("location");
                String formattedAddress = firstResult.path("formatted_address").asText();

                BigDecimal lat = new BigDecimal(location.path("lat").asText());
                BigDecimal lng = new BigDecimal(location.path("lng").asText());

                log.info("Geocodificado exitoso: {} -> lat={}, lng={}", direccionOriginal, lat, lng);
                return new CoordenadasDTO(lat, lng, formattedAddress);
            } else {
                log.warn("No se encontraron resultados para la dirección: {}", direccionOriginal);
                return new CoordenadasDTO(null, null, direccionOriginal);
            }
        } catch (Exception e) {
            log.error("Error parseando respuesta de geocoding para '{}': {}", direccionOriginal, e.getMessage());
            return new CoordenadasDTO(null, null, direccionOriginal);
        }
    }

    private DistanciaDTO parsearRespuesta(String json, String origen, String destino) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            
            // Validar status de la respuesta
            String status = root.path("status").asText();
            log.info("Status de Google Maps: {}", status);
            
            if (!"OK".equals(status)) {
                String errorMessage = root.path("error_message").asText("Sin mensaje de error");
                throw new RuntimeException("Error de Google Maps API. Status: " + status + ". Mensaje: " + errorMessage);
            }
            
            // Validar que rows existe y tiene elementos
            JsonNode rows = root.path("rows");
            if (rows.isEmpty() || rows.get(0) == null) {
                throw new RuntimeException("La respuesta de Google Maps no contiene datos de rutas");
            }
            
            JsonNode element = rows.get(0).path("elements").get(0);
            
            // Validar que el elemento tiene status OK
            String elementStatus = element.path("status").asText();
            if (!"OK".equals(elementStatus)) {
                throw new RuntimeException("No se pudo calcular la distancia. Status del elemento: " + elementStatus);
            }
            
            double distanciaMetros = element.path("distance").path("value").asDouble();
            double distanciaKm = distanciaMetros / 1000.0;
            
            String duracion = element.path("duration").path("text").asText();
            long duracionSegundos = element.path("duration").path("value").asLong();
            long duracionMinutos = duracionSegundos / 60;
            
            DistanciaDTO dto = new DistanciaDTO();
            dto.setOrigen(origen);
            dto.setDestino(destino);
            dto.setKilometros(distanciaKm);
            dto.setDuracionTexto(duracion);
            dto.setDuracionMinutos(duracionMinutos);
            
            return dto;
        } catch (Exception e) {
            log.error("Error al parsear respuesta de Google Maps", e);
            throw new RuntimeException("Error al parsear respuesta de Google Maps: " + e.getMessage(), e);
        }
    }
}
