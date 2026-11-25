package tp.backend.ms_solicitud.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tp.backend.ms_solicitud.client.ContenedorClientService;
import tp.backend.ms_solicitud.dto.CamionContenedorCompatibilidadDTO;
import tp.backend.ms_solicitud.dto.ContenedorDTO;
import tp.backend.ms_solicitud.entity.Camion;
import tp.backend.ms_solicitud.repository.CamionRepository;

@Service
@RequiredArgsConstructor
public class CamionService {

    private final CamionRepository camionRepository;
    private final ContenedorClientService contenedorClientService;

    public List<Camion> findAll() {
        return camionRepository.findAll();
    }

    public Camion findById(String dominio) {
        return camionRepository.findById(dominio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Camion no encontrado"));
    }

    public Camion create(Camion camion) {
        if (camion.getDominio() != null && camionRepository.existsById(camion.getDominio())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Camion con ese dominio ya existe");
        }
        return camionRepository.save(camion);
    }

    public Camion update(String dominio, Camion camion) {
        Camion existente = camionRepository.findById(dominio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Camion no encontrado"));

        // Actualizamos sólo los campos que vienen en el body (no nulos), para evitar
        // sobreescribir con null
        if (camion.getNombreTransportista() != null)
            existente.setNombreTransportista(camion.getNombreTransportista());
        if (camion.getTelefono() != null)
            existente.setTelefono(camion.getTelefono());
        if (camion.getCapacidadPeso() != null)
            existente.setCapacidadPeso(camion.getCapacidadPeso());
        if (camion.getCapacidadVolumen() != null)
            existente.setCapacidadVolumen(camion.getCapacidadVolumen());
        if (camion.getDisponibilidad() != null)
            existente.setDisponibilidad(camion.getDisponibilidad());
        if (camion.getConsumoPorKilometro() != null)
            existente.setConsumoPorKilometro(camion.getConsumoPorKilometro());
        if (camion.getCostoBasePorKm() != null)
            existente.setCostoBasePorKm(camion.getCostoBasePorKm());
        // `estaLibre` removed: availability is represented by `disponibilidad` and/or
        // computed from active tramos

        // dominio es la PK y se asigna por la path variable
        existente.setDominio(dominio);
        return camionRepository.save(existente);
    }

    public void delete(String dominio) {
        if (!camionRepository.existsById(dominio)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camion no encontrado");
        }
        camionRepository.deleteById(dominio);
    }

    /**
     * Verifica si un camión específico puede transportar un contenedor dado.
     * Consulta las dimensiones del contenedor desde ms-contenedor y compara con las
     * capacidades del camión.
     */
    public CamionContenedorCompatibilidadDTO puedeTransportar(String dominio, Integer idContenedor) {
        // Buscar el camión
        Camion camion = camionRepository.findById(dominio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Camion no encontrado con dominio: " + dominio));

        // Obtener el contenedor desde ms-contenedor (comunicación entre microservicios)
        ContenedorDTO contenedor = contenedorClientService.obtenerContenedor(idContenedor);

        // Validar restricciones de peso y volumen
        boolean cumplePeso = camion.getCapacidadPeso().compareTo(contenedor.getPeso()) >= 0;
        boolean cumpleVolumen = camion.getCapacidadVolumen().compareTo(contenedor.getVolumen()) >= 0;
        boolean puedeTransportar = cumplePeso && cumpleVolumen;

        // Construir mensaje detallado
        String mensaje = construirMensajeCompatibilidad(camion, contenedor, cumplePeso, cumpleVolumen,
                puedeTransportar);

        // Construir respuesta
        return CamionContenedorCompatibilidadDTO.builder()
                .puedeTransportar(puedeTransportar)
                .camion(CamionContenedorCompatibilidadDTO.CamionInfo.builder()
                        .dominio(camion.getDominio())
                        .capacidadPeso(camion.getCapacidadPeso())
                        .capacidadVolumen(camion.getCapacidadVolumen())
                        .costoBasePorKm(camion.getCostoBasePorKm())
                        .build())
                .contenedor(CamionContenedorCompatibilidadDTO.ContenedorInfo.builder()
                        .id(contenedor.getIdContenedor())
                        .peso(contenedor.getPeso())
                        .volumen(contenedor.getVolumen())
                        .build())
                .restricciones(CamionContenedorCompatibilidadDTO.Restricciones.builder()
                        .cumplePeso(cumplePeso)
                        .cumpleVolumen(cumpleVolumen)
                        .mensaje(mensaje)
                        .build())
                .build();
    }

    /**
     * Obtiene una lista de todos los camiones compatibles con un contenedor
     * específico.
     * Solo devuelve los camiones que SÍ pueden transportar el contenedor.
     */
    public List<CamionContenedorCompatibilidadDTO> obtenerCamionesCompatibles(Integer idContenedor) {
        // Obtener el contenedor desde ms-contenedor
        ContenedorDTO contenedor = contenedorClientService.obtenerContenedor(idContenedor);

        // Filtrar camiones compatibles
        return camionRepository.findAll().stream()
                .map(camion -> evaluarCompatibilidad(camion, contenedor))
                .filter(CamionContenedorCompatibilidadDTO::isPuedeTransportar)
                .collect(Collectors.toList());
    }

    /**
     * Evalúa la compatibilidad entre un camión y un contenedor.
     */
    private CamionContenedorCompatibilidadDTO evaluarCompatibilidad(Camion camion, ContenedorDTO contenedor) {
        boolean cumplePeso = camion.getCapacidadPeso().compareTo(contenedor.getPeso()) >= 0;
        boolean cumpleVolumen = camion.getCapacidadVolumen().compareTo(contenedor.getVolumen()) >= 0;
        boolean puedeTransportar = cumplePeso && cumpleVolumen;

        String mensaje = construirMensajeCompatibilidad(camion, contenedor, cumplePeso, cumpleVolumen,
                puedeTransportar);

        return CamionContenedorCompatibilidadDTO.builder()
                .puedeTransportar(puedeTransportar)
                .camion(CamionContenedorCompatibilidadDTO.CamionInfo.builder()
                        .dominio(camion.getDominio())
                        .capacidadPeso(camion.getCapacidadPeso())
                        .capacidadVolumen(camion.getCapacidadVolumen())
                        .costoBasePorKm(camion.getCostoBasePorKm())
                        .build())
                .contenedor(CamionContenedorCompatibilidadDTO.ContenedorInfo.builder()
                        .id(contenedor.getIdContenedor())
                        .peso(contenedor.getPeso())
                        .volumen(contenedor.getVolumen())
                        .build())
                .restricciones(CamionContenedorCompatibilidadDTO.Restricciones.builder()
                        .cumplePeso(cumplePeso)
                        .cumpleVolumen(cumpleVolumen)
                        .mensaje(mensaje)
                        .build())
                .build();
    }

    /**
     * Construye un mensaje descriptivo sobre la compatibilidad.
     */
    private String construirMensajeCompatibilidad(Camion camion, ContenedorDTO contenedor,
            boolean cumplePeso, boolean cumpleVolumen,
            boolean puedeTransportar) {
        if (puedeTransportar) {
            return String.format("✓ El camión %s puede transportar el contenedor %d",
                    camion.getDominio(), contenedor.getIdContenedor());
        } else {
            StringBuilder sb = new StringBuilder("✗ Incompatible: ");
            if (!cumplePeso) {
                sb.append(String.format("excede peso (%.2f kg > %.2f kg)",
                        contenedor.getPeso(), camion.getCapacidadPeso()));
            }
            if (!cumpleVolumen) {
                if (!cumplePeso)
                    sb.append(", ");
                sb.append(String.format("excede volumen (%.2f m³ > %.2f m³)",
                        contenedor.getVolumen(), camion.getCapacidadVolumen()));
            }
            return sb.toString();
        }
    }
}
