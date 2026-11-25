package tp.backend.ms_solicitud.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tp.backend.ms_solicitud.client.DepositoClientService;
import tp.backend.ms_solicitud.client.TarifaClientService;
import tp.backend.ms_solicitud.dto.DatosCostoDTO;
import tp.backend.ms_solicitud.dto.DepositoDTO;
import tp.backend.ms_solicitud.dto.RutaTentativaDTO;
import tp.backend.ms_solicitud.dto.RutasTentativasResponseDTO;
import tp.backend.ms_solicitud.dto.TarifaDTO;
import tp.backend.ms_solicitud.dto.TramoTentativoDTO;
import tp.backend.ms_solicitud.entity.Camion;
import tp.backend.ms_solicitud.geo.DistanciaDTO;
import tp.backend.ms_solicitud.geo.GeoService;
import tp.backend.ms_solicitud.repository.CamionRepository;

/**
 * Servicio para calcular rutas tentativas SIN PERSISTIR en BD.
 * Genera combinaciones de rutas (con 0, 1, 2 depósitos intermedios), 
 * calcula distancias/costos reales y devuelve opciones al operador.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RutaTentativaService {
    private final CamionRepository camionRepository;
    private final GeoService geoService;
    private final DepositoClientService depositoClient;
    private final TarifaClientService tarifaClient;

    // Máximo número de depósitos intermedios a considerar
    private static final int MAX_DEPOSITOS_INTERMEDIOS = 2;

    /**
     * Calcula todas las rutas tentativas para una solicitud.
     * No persiste nada, solo devuelve JSON con opciones.
     * 
     * @param solicitudId ID de la solicitud
     * @param origen Origen (ya validado y obtenido de la solicitud)
     * @param destino Destino (ya validado y obtenido de la solicitud)
     */
    public RutasTentativasResponseDTO calcularRutasTentativas(Integer solicitudId, 
                                                               String origen, 
                                                               String destino) {
        log.info("Calculando rutas tentativas para solicitud {} (origen: {}, destino: {})", 
                 solicitudId, origen, destino);

        // 1. Obtener tarifa vigente y datos de costo
        TarifaDTO tarifa = tarifaClient.obtenerTarifaVigente();
        DatosCostoDTO datosCosto = obtenerDatosCosto(tarifa);

        // 4. Obtener depósitos disponibles
        List<DepositoDTO> depositos = obtenerDepositos();
        log.info("Depósitos disponibles: {}", depositos.size());

        // 5. Generar todas las combinaciones de rutas
        List<RutaTentativaDTO> rutasTentativas = new ArrayList<>();
        int numeroOpcion = 1;

        // Ruta directa (0 depósitos)
        RutaTentativaDTO rutaDirecta = calcularRutaDirecta(numeroOpcion++, origen, destino, datosCosto);
        rutasTentativas.add(rutaDirecta);

        // Ruta con 1 depósito (solo el primero)
        if (!depositos.isEmpty()) {
            RutaTentativaDTO ruta = calcularRutaConUnDeposito(numeroOpcion++, origen, destino, depositos.get(0), datosCosto);
            rutasTentativas.add(ruta);
        }

        // Ruta con 2 depósitos (solo los dos primeros)
        if (depositos.size() >= 2 && MAX_DEPOSITOS_INTERMEDIOS >= 2) {
            RutaTentativaDTO ruta = calcularRutaConDosDepositos(
                numeroOpcion++, origen, destino, depositos.get(0), depositos.get(1), datosCosto
            );
            rutasTentativas.add(ruta);
        }

        // 6. Construir respuesta
        return RutasTentativasResponseDTO.builder()
                .solicitudId(solicitudId)
                .origen(origen)
                .destino(destino)
                .cantidadOpcionesCalculadas(rutasTentativas.size())
                .rutas(rutasTentativas)
                .mensaje("Rutas calculadas exitosamente. El operador debe elegir una opción.")
                .build();
    }

    /**
     * Calcula una ruta directa (origen → destino).
     */
    private RutaTentativaDTO calcularRutaDirecta(int numeroOpcion, String origen, String destino, DatosCostoDTO datosCosto) {
        log.info("Calculando ruta directa: {} → {}", origen, destino);

        DistanciaDTO distancia = geoService.calcularDistancia(origen, destino);
        BigDecimal costoEstimado = calcularCosto(distancia.getKilometros(), datosCosto);

        // Calcular fechas estimadas: inicio ahora, fin = inicio + duración
        java.time.LocalDateTime fechaInicioEstimada = java.time.LocalDateTime.now();
        java.time.LocalDateTime fechaFinEstimada = fechaInicioEstimada.plusMinutes(distancia.getDuracionMinutos());

        TramoTentativoDTO tramo = TramoTentativoDTO.builder()
                .orden(1)
                .origenNombre(origen)
                .destinoNombre(destino)
                .origenDepositoId(null)  // Ni origen ni destino son depósitos
                .destinoDepositoId(null)
                .tipo("origen-destino")
                .distanciaKm(BigDecimal.valueOf(distancia.getKilometros()))
                .duracionTexto(distancia.getDuracionTexto())
                .duracionEstimadaMinutos(distancia.getDuracionMinutos())
                .costoEstimado(costoEstimado)
                .fechaHoraInicioEstimada(fechaInicioEstimada)
                .fechaHoraFinEstimada(fechaFinEstimada)
                .build();

        return RutaTentativaDTO.builder()
                .numeroOpcion(numeroOpcion)
                .cantidadTramos(1)
                .cantidadDepositos(0)
                .distanciaTotalKm(BigDecimal.valueOf(distancia.getKilometros()))
                .duracionTotalTexto(distancia.getDuracionTexto())
                .costoTotalEstimado(costoEstimado)
                .tramos(Arrays.asList(tramo))
                .descripcion(String.format("Ruta directa: %s → %s", origen, destino))
                .build();
    }

    /**
     * Calcula una ruta con 1 depósito intermedio (origen → depósito → destino).
     */
    private RutaTentativaDTO calcularRutaConUnDeposito(int numeroOpcion, String origen, String destino, 
                                                        DepositoDTO deposito, DatosCostoDTO datosCosto) {
        log.info("Calculando ruta con 1 depósito: {} → {} → {}", origen, deposito.getNombre(), destino);

        String direccionDeposito = obtenerDireccionDeposito(deposito);

        // Tramo 1: origen → depósito
        DistanciaDTO distancia1 = geoService.calcularDistancia(origen, direccionDeposito);
        BigDecimal costo1 = calcularCosto(distancia1.getKilometros(), datosCosto);

        java.time.LocalDateTime fechaInicioTramo1 = java.time.LocalDateTime.now();
        java.time.LocalDateTime fechaFinTramo1 = fechaInicioTramo1.plusMinutes(distancia1.getDuracionMinutos());

        TramoTentativoDTO tramo1 = TramoTentativoDTO.builder()
                .orden(1)
                .origenNombre(origen)
                .destinoNombre(deposito.getNombre())
                .origenDepositoId(null)  // El origen no es un depósito
                .destinoDepositoId(deposito.getId().intValue())  // El destino es el depósito
                .tipo("origen-deposito")
                .distanciaKm(BigDecimal.valueOf(distancia1.getKilometros()))
                .duracionTexto(distancia1.getDuracionTexto())
                .duracionEstimadaMinutos(distancia1.getDuracionMinutos())
                .costoEstimado(costo1)
                .fechaHoraInicioEstimada(fechaInicioTramo1)
                .fechaHoraFinEstimada(fechaFinTramo1)
                .build();

        // Tramo 2: depósito → destino
        // Asumimos 1 día de estadía en depósito (24 horas)
        DistanciaDTO distancia2 = geoService.calcularDistancia(direccionDeposito, destino);
        BigDecimal costo2 = calcularCosto(distancia2.getKilometros(), datosCosto);

        java.time.LocalDateTime fechaInicioTramo2 = fechaFinTramo1.plusHours(24); // +24h de estadía
        java.time.LocalDateTime fechaFinTramo2 = fechaInicioTramo2.plusMinutes(distancia2.getDuracionMinutos());

        TramoTentativoDTO tramo2 = TramoTentativoDTO.builder()
                .orden(2)
                .origenNombre(deposito.getNombre())
                .destinoNombre(destino)
                .origenDepositoId(deposito.getId().intValue())  // El origen es el depósito
                .destinoDepositoId(null)  // El destino no es un depósito
                .tipo("deposito-destino")
                .distanciaKm(BigDecimal.valueOf(distancia2.getKilometros()))
                .duracionTexto(distancia2.getDuracionTexto())
                .duracionEstimadaMinutos(distancia2.getDuracionMinutos())
                .costoEstimado(costo2)
                .fechaHoraInicioEstimada(fechaInicioTramo2)
                .fechaHoraFinEstimada(fechaFinTramo2)
                .build();

        BigDecimal distanciaTotal = BigDecimal.valueOf(distancia1.getKilometros() + distancia2.getKilometros());
        BigDecimal costoTotal = costo1.add(costo2);
        // Aquí podrías sumar costo de estadía en depósito si aplica
        if (deposito.getCostoDiaDeposito() != null) {
            costoTotal = costoTotal.add(deposito.getCostoDiaDeposito());
        }

        return RutaTentativaDTO.builder()
                .numeroOpcion(numeroOpcion)
                .cantidadTramos(2)
                .cantidadDepositos(1)
                .distanciaTotalKm(distanciaTotal)
                .duracionTotalTexto(distancia1.getDuracionTexto() + " + " + distancia2.getDuracionTexto())
                .costoTotalEstimado(costoTotal)
                .tramos(Arrays.asList(tramo1, tramo2))
                .descripcion(String.format("Ruta con 1 depósito: %s → %s → %s", origen, deposito.getNombre(), destino))
                .build();
    }

    /**
     * Calcula una ruta con 2 depósitos intermedios (origen → dep1 → dep2 → destino).
     */
    private RutaTentativaDTO calcularRutaConDosDepositos(int numeroOpcion, String origen, String destino,
                                                          DepositoDTO deposito1, DepositoDTO deposito2, 
                                                          DatosCostoDTO datosCosto) {
        log.info("Calculando ruta con 2 depósitos: {} → {} → {} → {}", 
                 origen, deposito1.getNombre(), deposito2.getNombre(), destino);

        String direccionDep1 = obtenerDireccionDeposito(deposito1);
        String direccionDep2 = obtenerDireccionDeposito(deposito2);

        // Tramo 1: origen → dep1
        DistanciaDTO distancia1 = geoService.calcularDistancia(origen, direccionDep1);
        BigDecimal costo1 = calcularCosto(distancia1.getKilometros(), datosCosto);

        java.time.LocalDateTime fechaInicioTramo1 = java.time.LocalDateTime.now();
        java.time.LocalDateTime fechaFinTramo1 = fechaInicioTramo1.plusMinutes(distancia1.getDuracionMinutos());

        TramoTentativoDTO tramo1 = TramoTentativoDTO.builder()
                .orden(1)
                .origenNombre(origen)
                .destinoNombre(deposito1.getNombre())
                .origenDepositoId(null)  // El origen no es un depósito
                .destinoDepositoId(deposito1.getId().intValue())  // El destino es el depósito 1
                .tipo("origen-deposito")
                .distanciaKm(BigDecimal.valueOf(distancia1.getKilometros()))
                .duracionTexto(distancia1.getDuracionTexto())
                .duracionEstimadaMinutos(distancia1.getDuracionMinutos())
                .costoEstimado(costo1)
                .fechaHoraInicioEstimada(fechaInicioTramo1)
                .fechaHoraFinEstimada(fechaFinTramo1)
                .build();

        // Tramo 2: dep1 → dep2 (asumimos 1 día de estadía)
        DistanciaDTO distancia2 = geoService.calcularDistancia(direccionDep1, direccionDep2);
        BigDecimal costo2 = calcularCosto(distancia2.getKilometros(), datosCosto);

        java.time.LocalDateTime fechaInicioTramo2 = fechaFinTramo1.plusHours(24); // +24h estadía
        java.time.LocalDateTime fechaFinTramo2 = fechaInicioTramo2.plusMinutes(distancia2.getDuracionMinutos());

        TramoTentativoDTO tramo2 = TramoTentativoDTO.builder()
                .orden(2)
                .origenNombre(deposito1.getNombre())
                .destinoNombre(deposito2.getNombre())
                .origenDepositoId(deposito1.getId().intValue())  // El origen es el depósito 1
                .destinoDepositoId(deposito2.getId().intValue())  // El destino es el depósito 2
                .tipo("deposito-deposito")
                .distanciaKm(BigDecimal.valueOf(distancia2.getKilometros()))
                .duracionTexto(distancia2.getDuracionTexto())
                .duracionEstimadaMinutos(distancia2.getDuracionMinutos())
                .costoEstimado(costo2)
                .fechaHoraInicioEstimada(fechaInicioTramo2)
                .fechaHoraFinEstimada(fechaFinTramo2)
                .build();

        // Tramo 3: dep2 → destino (asumimos 1 día de estadía)
        DistanciaDTO distancia3 = geoService.calcularDistancia(direccionDep2, destino);
        BigDecimal costo3 = calcularCosto(distancia3.getKilometros(), datosCosto);

        java.time.LocalDateTime fechaInicioTramo3 = fechaFinTramo2.plusHours(24); // +24h estadía
        java.time.LocalDateTime fechaFinTramo3 = fechaInicioTramo3.plusMinutes(distancia3.getDuracionMinutos());

        TramoTentativoDTO tramo3 = TramoTentativoDTO.builder()
                .orden(3)
                .origenNombre(deposito2.getNombre())
                .destinoNombre(destino)
                .origenDepositoId(deposito2.getId().intValue())  // El origen es el depósito 2
                .destinoDepositoId(null)  // El destino no es un depósito
                .tipo("deposito-destino")
                .distanciaKm(BigDecimal.valueOf(distancia3.getKilometros()))
                .duracionTexto(distancia3.getDuracionTexto())
                .duracionEstimadaMinutos(distancia3.getDuracionMinutos())
                .costoEstimado(costo3)
                .fechaHoraInicioEstimada(fechaInicioTramo3)
                .fechaHoraFinEstimada(fechaFinTramo3)
                .build();

        BigDecimal distanciaTotal = BigDecimal.valueOf(
            distancia1.getKilometros() + distancia2.getKilometros() + distancia3.getKilometros()
        );
        BigDecimal costoTotal = costo1.add(costo2).add(costo3);

        // Sumar costos de estadía si aplican
        if (deposito1.getCostoDiaDeposito() != null) {
            costoTotal = costoTotal.add(deposito1.getCostoDiaDeposito());
        }
        if (deposito2.getCostoDiaDeposito() != null) {
            costoTotal = costoTotal.add(deposito2.getCostoDiaDeposito());
        }

        return RutaTentativaDTO.builder()
                .numeroOpcion(numeroOpcion)
                .cantidadTramos(3)
                .cantidadDepositos(2)
                .distanciaTotalKm(distanciaTotal)
                .duracionTotalTexto(distancia1.getDuracionTexto() + " + " + 
                                    distancia2.getDuracionTexto() + " + " + 
                                    distancia3.getDuracionTexto())
                .costoTotalEstimado(costoTotal)
                .tramos(Arrays.asList(tramo1, tramo2, tramo3))
                .descripcion(String.format("Ruta con 2 depósitos: %s → %s → %s → %s", 
                                          origen, deposito1.getNombre(), deposito2.getNombre(), destino))
                .build();
    }

    /**
     * Calcula el costo de un tramo en base a los kilómetros y los datos de costo.
     * Fórmula: costoEstimado = (distancia × costoBaseKm) + (distancia × consumoCombustible × valorLitroCombustible)
     */
    private BigDecimal calcularCosto(double kilometros, DatosCostoDTO datosCosto) {
        BigDecimal km = BigDecimal.valueOf(kilometros);
        
        // Costo base: distancia × costoBasePorKm
        BigDecimal costoBase = km.multiply(datosCosto.getCostoBasePorKm());
        
        // Costo combustible: distancia × consumo × valorLitro
        BigDecimal costoCombustible = km
                .multiply(datosCosto.getConsumoPromedioPorKm())
                .multiply(datosCosto.getValorLitroCombustible());
        
        // Total
        BigDecimal costoTotal = costoBase.add(costoCombustible);
        
        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Obtiene la dirección de un depósito para Google Maps.
     */
    private String obtenerDireccionDeposito(DepositoDTO deposito) {
        if (deposito.getLatitud() != null && deposito.getLongitud() != null) {
            return deposito.getLatitud() + "," + deposito.getLongitud();
        }
        return deposito.getDireccion() != null ? deposito.getDireccion() : deposito.getNombre();
    }

    /**
     * Obtiene la lista de depósitos disponibles desde ms-contenedor.
     */
    private List<DepositoDTO> obtenerDepositos() {
        try {
            List<DepositoDTO> depositos = depositoClient.obtenerTodosLosDepositos();
            log.info("Depósitos obtenidos desde ms-contenedor: {}", depositos.size());
            return depositos;
        } catch (Exception e) {
            log.error("Error al obtener depósitos desde ms-contenedor: {}", e.getMessage());
            // Fallback: devolver lista vacía si el servicio no está disponible
            return List.of();
        }
    }

    /**
     * Obtiene los datos necesarios para calcular costos (tarifa y promedios de camiones).
     * @throws ResponseStatusException si no hay camiones registrados en el sistema
     */
    private DatosCostoDTO obtenerDatosCosto(TarifaDTO tarifa) {
        List<Camion> camiones = camionRepository.findAll();
        
        if (camiones.isEmpty()) {
            log.error("❌ No se pueden calcular rutas: no hay camiones registrados en el sistema");
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED,
                    "No se pueden calcular rutas tentativas. Debe haber al menos un camión registrado en el sistema para calcular costos estimados.");
        }

        // Calcular promedios de consumo y costo base
        BigDecimal sumaConsumo = BigDecimal.ZERO; //para iniciar en 0 y tener presicion
        BigDecimal sumaCostoBase = BigDecimal.ZERO;
        
        for (Camion camion : camiones) {
            if (camion.getConsumoPorKilometro() != null) {
                sumaConsumo = sumaConsumo.add(camion.getConsumoPorKilometro());
            }
            if (camion.getCostoBasePorKm() != null) {
                sumaCostoBase = sumaCostoBase.add(camion.getCostoBasePorKm());
            }
        }

        BigDecimal consumoPromedio = sumaConsumo.divide(
                BigDecimal.valueOf(camiones.size()), 4, RoundingMode.HALF_UP);
        BigDecimal costoBasePromedio = sumaCostoBase.divide(
                BigDecimal.valueOf(camiones.size()), 2, RoundingMode.HALF_UP);

        log.info("Datos de costo calculados - Consumo promedio: {} L/km, Costo base promedio: {} $/km, Valor combustible: {} $/L",
                consumoPromedio, costoBasePromedio, tarifa.getValorLitroCombustible());

        return DatosCostoDTO.builder()
                .valorLitroCombustible(tarifa.getValorLitroCombustible())
                .consumoPromedioPorKm(consumoPromedio)
                .costoBasePorKm(costoBasePromedio)
                .build();
    }
}
