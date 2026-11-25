package tp.backend.ms_solicitud.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tp.backend.ms_solicitud.client.DepositoClientService;
import tp.backend.ms_solicitud.client.TarifaClientService;
import tp.backend.ms_solicitud.dto.DepositoDTO;
import tp.backend.ms_solicitud.dto.RutaTentativaDTO;
import tp.backend.ms_solicitud.dto.TarifaDTO;
import tp.backend.ms_solicitud.entity.EstadoSolicitudEnum;
import tp.backend.ms_solicitud.entity.EstadoTramoEnum;
import tp.backend.ms_solicitud.entity.Punto;
import tp.backend.ms_solicitud.entity.Ruta;
import tp.backend.ms_solicitud.entity.Solicitud;
import tp.backend.ms_solicitud.entity.Tipo;
import tp.backend.ms_solicitud.entity.Tramo;
import tp.backend.ms_solicitud.geo.CoordenadasDTO;
import tp.backend.ms_solicitud.geo.GeoService;
import tp.backend.ms_solicitud.repository.PuntoRepository;
import tp.backend.ms_solicitud.repository.RutaRepository;
import tp.backend.ms_solicitud.repository.SolicitudRepository;
import tp.backend.ms_solicitud.repository.TipoRepository;
import tp.backend.ms_solicitud.repository.TramoRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final PuntoRepository puntoRepository;
    private final TipoRepository tipoRepository;
    private final DepositoClientService depositoClientService;
    private final TarifaClientService tarifaClientService;
    private final GeoService geoService;

    public List<Solicitud> findAll() {
        return solicitudRepository.findAll();
    }

    public Solicitud findById(Integer id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitud no encontrada con id " + id));
    }

    @Transactional // Para operaciones de escritura
    public Solicitud create(Solicitud solicitud) {
        solicitud.setId(null);
        // El estado inicial "Borrador" (1) ya está establecido por defecto en la entidad
        return solicitudRepository.save(solicitud);
    }

    @Transactional 
    public Solicitud update(Integer id, Solicitud solicitud) {
        Solicitud existente = solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Solicitud no encontrada con id " + id));

        existente.setContenedorId(solicitud.getContenedorId());
        existente.setClienteId(solicitud.getClienteId());
        existente.setCostoEstimado(solicitud.getCostoEstimado());
        existente.setTiempoEstimado(solicitud.getTiempoEstimado());
        existente.setCostoFinal(solicitud.getCostoFinal());
        existente.setTiempoReal(solicitud.getTiempoReal());
        existente.setEstado(solicitud.getEstado());
        existente.setOrigen(solicitud.getOrigen());

        return solicitudRepository.save(existente);
    }

    @Transactional
    public void delete(Integer id) {
        if (!solicitudRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada con id " + id);
        }
        solicitudRepository.deleteById(id);
    }

    public List<Solicitud> findByCliente(Integer clienteId) {
        return solicitudRepository.findByClienteId(clienteId);
    }

    public List<Solicitud> findByEstado(EstadoSolicitudEnum estado) {
        return solicitudRepository.findByEstado(estado);
    }

    /**
     * Asigna una ruta tentativa seleccionada por el operador a la solicitud.
     * Persiste la ruta y sus tramos en la BD.
     * 
     * @param solicitudId ID de la solicitud
     * @param rutaTentativa Ruta tentativa seleccionada (con sus tramos calculados)
     * @return Ruta persistida
     */
    @Transactional
    public Ruta asignarRuta(Integer solicitudId, RutaTentativaDTO rutaTentativa) {
        // 1. Verificar que la solicitud existe
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        // 2. Verificar si ya tiene una ruta asignada y eliminarla
        if (solicitud.getRuta() != null) {
            // Eliminar todos los tramos de la ruta existente
            tramoRepository.deleteByRuta(solicitud.getRuta());
            rutaRepository.delete(solicitud.getRuta());
            solicitud.setRuta(null); // Limpiar la referencia
        }

        // 3. Crear y persistir la Ruta (sin asignar solicitud todavía)
        Ruta ruta = new Ruta();
        ruta.setCantidadTramos(rutaTentativa.getCantidadTramos());
        ruta.setCantidadDepositos(rutaTentativa.getCantidadDepositos());
        
        Ruta rutaGuardada = rutaRepository.save(ruta);

        // 4. Asignar la ruta a la solicitud (Solicitud es el owner de la relación)
        solicitud.setRuta(rutaGuardada);

        // 5. Crear y persistir cada Tramo con estado ESTIMADO
        rutaTentativa.getTramos().forEach(tramoDTO -> {
            // Crear o buscar puntos origen y destino
            Punto puntoOrigen = crearOBuscarPunto(tramoDTO.getOrigenNombre(), tramoDTO.getOrigenDepositoId());
            Punto puntoDestino = crearOBuscarPunto(tramoDTO.getDestinoNombre(), tramoDTO.getDestinoDepositoId());

            // Buscar tipo de tramo por nombre
            Tipo tipo = buscarOCrearTipo(tramoDTO.getTipo());

            // Crear tramo
            Tramo tramo = new Tramo();
            tramo.setRuta(rutaGuardada);
            tramo.setOrigen(puntoOrigen);
            tramo.setDestino(puntoDestino);
            tramo.setTipo(tipo);
            tramo.setKilometrosTramo(tramoDTO.getDistanciaKm());
            tramo.setCostoAproximado(tramoDTO.getCostoEstimado());
            tramo.setEstado(EstadoTramoEnum.ESTIMADO);
            // Asignar fechas estimadas desde el DTO
            tramo.setFechaHoraInicioEstimada(tramoDTO.getFechaHoraInicioEstimada());
            tramo.setFechaHoraFinEstimada(tramoDTO.getFechaHoraFinEstimada());

            tramoRepository.save(tramo);
        });

        // 6. Actualizar solicitud con costo estimado y cambiar estado a PROGRAMADA
        solicitud.setCostoEstimado(rutaTentativa.getCostoTotalEstimado().doubleValue());
        solicitud.setEstado(EstadoSolicitudEnum.PROGRAMADA);
        solicitudRepository.save(solicitud);

        return rutaGuardada;
    }

    /**
     * Calcula el costo real (final) de una solicitud basándose en:
     * - Cargo de gestión fijo por tramo
     * - Costo por kilómetro de cada camión asignado
     * - Costo de combustible (km × consumo × valor litro)
     * - Costo de estadía en depósitos
     * 
     * También calcula el tiempo real: fecha fin del último tramo.
     * 
     * Actualiza solicitud.costoFinal, solicitud.tiempoReal y tramo.costoReal para cada tramo.
     * 
     * @param solicitudId ID de la solicitud
     * @throws ResponseStatusException si no hay ruta asignada o faltan datos
     */
    @Transactional
    public void calcularCostoReal(Integer solicitudId) {
        log.info("🧮 Iniciando cálculo de costo real para solicitud {}", solicitudId);
        
        // 1. Buscar solicitud y validar que exista
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Solicitud no encontrada con id " + solicitudId));
        
        // 2. Obtener la única ruta asignada a la solicitud
        Ruta ruta = solicitud.getRuta();
        if (ruta == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede calcular costo real: solicitud sin ruta asignada");
        }
        
        log.info("📍 Ruta encontrada: {} tramos, {} depósitos", 
            ruta.getCantidadTramos(), ruta.getCantidadDepositos());
        
        // 3. Obtener todos los tramos de la ruta
        List<Tramo> tramos = tramoRepository.findAll().stream()
                .filter(t -> t.getRuta().getId().equals(ruta.getId()))
                .toList();
        
        if (tramos.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede calcular costo real: ruta sin tramos");
        }
        
        // 4. Validar que todos los tramos tengan camión asignado
        List<Tramo> tramosSinCamion = tramos.stream()
                .filter(t -> t.getCamion() == null)
                .toList();
        
        if (!tramosSinCamion.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("No se puede calcular costo real: hay %d tramo(s) sin camión asignado",
                            tramosSinCamion.size()));
        }
        
        log.info("✅ Todos los tramos tienen camión asignado");
        
        // 5. Obtener tarifa vigente
        TarifaDTO tarifa = tarifaClientService.obtenerTarifaVigente();
        log.info("💵 Tarifa vigente: valorLitro=${}, cargoGestion=${}/tramo", 
            tarifa.getValorLitroCombustible(),
            tarifa.getCargoGestionPorTramo());
        
        // 6. CALCULAR COMPONENTES DEL COSTO:
        
        // A) Cargo de Gestión (fijo por cantidad de tramos)
        BigDecimal cargoGestion = tarifa.getCargoGestionPorTramo()
                .multiply(new BigDecimal(tramos.size()));
        log.info("📋 Cargo de gestión: {} tramos × ${} = ${}", 
            tramos.size(),
            tarifa.getCargoGestionPorTramo(),
            cargoGestion);
        
        // B) Costo por kilómetro (usando camión asignado a cada tramo)
        BigDecimal costoKilometraje = BigDecimal.ZERO;
        for (Tramo tramo : tramos) {
            BigDecimal costoKmTramo = tramo.getKilometrosTramo()
                    .multiply(tramo.getCamion().getCostoBasePorKm());
            costoKilometraje = costoKilometraje.add(costoKmTramo);
            
            log.debug("  🚛 Tramo {}: {} km × ${}/km = ${}", 
                tramo.getId(),
                tramo.getKilometrosTramo(),
                tramo.getCamion().getCostoBasePorKm(),
                costoKmTramo);
        }
        log.info("🛣️  Costo kilometraje total: ${}", costoKilometraje);
        
        // C) Costo combustible (km × consumo × valor litro)
        BigDecimal costoCombustible = BigDecimal.ZERO;
        for (Tramo tramo : tramos) {
            BigDecimal costoCombustibleTramo = tramo.getKilometrosTramo()
                    .multiply(tramo.getCamion().getConsumoPorKilometro())
                    .multiply(tarifa.getValorLitroCombustible());
            costoCombustible = costoCombustible.add(costoCombustibleTramo);
            
            log.debug("  ⛽ Tramo {}: {} km × {} L/km × ${}/L = ${}", 
                tramo.getId(),
                tramo.getKilometrosTramo(),
                tramo.getCamion().getConsumoPorKilometro(),
                tarifa.getValorLitroCombustible(),
                costoCombustibleTramo);
        }
        log.info("⛽ Costo combustible total: ${}", costoCombustible);
        
        // D) Costo estadía en depósitos
        BigDecimal costoEstadia = calcularCostoEstadia(tramos);
        
        // 7. CALCULAR TIEMPO REAL (fecha inicio primer tramo → fecha fin último tramo)
        java.sql.Date tiempoReal = calcularTiempoReal(tramos);
        
        // 8. SUMAR TODOS LOS COMPONENTES
        BigDecimal costoTotal = cargoGestion
                .add(costoKilometraje)
                .add(costoCombustible)
                .add(costoEstadia);
        
        log.info("💰 COSTO TOTAL REAL: ${} (gestión: ${}, km: ${}, combustible: ${}, estadía: ${})",
            costoTotal,
            cargoGestion,
            costoKilometraje,
            costoCombustible,
            costoEstadia);
        
        // 9. Actualizar solicitud con costo final y tiempo real
        solicitud.setCostoFinal(costoTotal.doubleValue());
        solicitud.setTiempoReal(tiempoReal);
        solicitudRepository.save(solicitud);
        
        log.info("⏱️  Tiempo real de la solicitud: {}", tiempoReal);
        
        // 10. Actualizar cada tramo con su costo real individual
        for (Tramo tramo : tramos) {
            BigDecimal costoRealTramo = calcularCostoRealTramo(tramo, tarifa);
            tramo.setCostoReal(costoRealTramo);
            tramoRepository.save(tramo);
            
            log.debug("  📝 Tramo {}: costo real = ${}", tramo.getId(), costoRealTramo);
        }
        
        log.info("✅ Cálculo de costo real completado para solicitud {}", solicitudId);
    }

    /**
     * Calcula el costo real de un tramo individual.
     * Incluye: kilometraje + combustible (no incluye cargo gestión ni estadía).
     * 
     * @param tramo Tramo a calcular
     * @param tarifa Tarifa vigente
     * @return Costo real del tramo
     */
    private BigDecimal calcularCostoRealTramo(Tramo tramo, TarifaDTO tarifa) {
        if (tramo.getCamion() == null) {
            return BigDecimal.ZERO;
        }
        
        // Costo kilometraje
        BigDecimal costoKm = tramo.getKilometrosTramo()
                .multiply(tramo.getCamion().getCostoBasePorKm());
        
        // Costo combustible
        BigDecimal costoCombustible = tramo.getKilometrosTramo()
                .multiply(tramo.getCamion().getConsumoPorKilometro())
                .multiply(tarifa.getValorLitroCombustible());
        
        return costoKm.add(costoCombustible);
    }

    /**
     * Calcula el tiempo real total de la solicitud.
     * Toma la fecha de inicio del primer tramo y la fecha de fin del último tramo.
     * 
     * @param tramos Lista de tramos de la ruta
     * @return Fecha de finalización (tiempo real) como java.sql.Date
     * @throws ResponseStatusException si algún tramo no tiene fechas reales
     */
    private java.sql.Date calcularTiempoReal(List<Tramo> tramos) {
        // Validar que todos los tramos tengan fechas reales
        for (Tramo tramo : tramos) {
            if (tramo.getFechaHoraInicio() == null || tramo.getFechaHoraFin() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se puede calcular tiempo real: tramo sin fechas reales asignadas");
            }
        }
        
        // Encontrar fecha de inicio más temprana (primer tramo)
        java.time.LocalDateTime fechaInicio = tramos.stream()
                .map(Tramo::getFechaHoraInicio)
                .min(java.time.LocalDateTime::compareTo)
                .orElseThrow();
        
        // Encontrar fecha de fin más tardía (último tramo)
        java.time.LocalDateTime fechaFin = tramos.stream()
                .map(Tramo::getFechaHoraFin)
                .max(java.time.LocalDateTime::compareTo)
                .orElseThrow();
        
        log.info("📅 Tiempo real calculado: inicio={}, fin={}", fechaInicio, fechaFin);
        
        // Retornar la fecha de fin como java.sql.Date
        return java.sql.Date.valueOf(fechaFin.toLocalDate());
    }

    /**
     * Busca un punto por dirección o lo crea si no existe.
     * Si depositoId no es null, lo asigna al punto.
     */
    private Punto crearOBuscarPunto(String direccion, Integer depositoId) {
        // Por simplicidad, buscar por dirección exacta
        List<Punto> puntos = puntoRepository.findAll();
        return puntos.stream()
                .filter(p -> direccion.equals(p.getDireccionTextual()))
                .findFirst()
                .orElseGet(() -> {
                    Punto nuevo = new Punto();
                    nuevo.setDireccionTextual(direccion);
                    nuevo.setDepositoId(depositoId);  // Asignar el depositoId si existe
                    
                    // 🌍 Geocodificar la dirección para obtener coordenadas
                    try {
                        CoordenadasDTO coordenadas = geoService.geocodificar(direccion);
                        if (coordenadas != null) {
                            nuevo.setLatitud(coordenadas.getLatitud());
                            nuevo.setLongitud(coordenadas.getLongitud());
                            log.info("📍 Punto geocodificado: '{}' -> lat={}, lng={}", 
                                direccion, coordenadas.getLatitud(), coordenadas.getLongitud());
                        } else {
                            log.warn("⚠️ No se obtuvieron coordenadas para: {}", direccion);
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Error al geocodificar '{}': {}. Continuando sin coordenadas.", 
                            direccion, e.getMessage());
                    }
                    
                    return puntoRepository.save(nuevo);
                });
    }

    /**
     * Busca un tipo por nombre o lo crea si no existe.
     */
    private Tipo buscarOCrearTipo(String nombre) {
        List<Tipo> tipos = tipoRepository.findAll();
        return tipos.stream()
                .filter(t -> nombre.equals(t.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Tipo nuevo = new Tipo();
                    nuevo.setNombre(nombre);
                    return tipoRepository.save(nuevo);
                });
    }

    /**
     * Calcula el costo de estadía en depósitos para una lista de tramos.
     * Solo considera tramos cuyo destino sea un depósito (punto.depositoId != null).
     * Consulta ms-contenedor para obtener el costo diario de cada depósito.
     * 
     * @param tramos Lista de tramos a analizar
     * @return Costo total de estadía en BigDecimal
     */
    private BigDecimal calcularCostoEstadia(List<Tramo> tramos) {
        log.info("🏢 Calculando costo de estadía para {} tramos", tramos.size());
        
        // Obtener todos los depósitos desde ms-contenedor
        List<DepositoDTO> depositos = depositoClientService.obtenerTodosLosDepositos();
        
        BigDecimal costoTotal = BigDecimal.ZERO;
        
        for (Tramo tramo : tramos) {
            // Verificar si el destino del tramo es un depósito
            Punto destino = tramo.getDestino();
            
            if (destino.getDepositoId() == null) {
                log.debug("  ⏭️  Tramo {} → {}: destino no es depósito, skip", 
                    tramo.getOrigen().getDireccionTextual(), 
                    destino.getDireccionTextual());
                continue; // No es un depósito, siguiente tramo
            }
            
            // Buscar el depósito correspondiente en ms-contenedor
            DepositoDTO deposito = depositos.stream()
                .filter(d -> d.getIdDeposito().equals(destino.getDepositoId().longValue()))
                .findFirst()
                .orElse(null);
            
            if (deposito == null) {
                log.warn("⚠️  Depósito con ID {} no encontrado en ms-contenedor", destino.getDepositoId());
                continue;
            }
            
            // Calcular días de estadía
            if (tramo.getFechaHoraInicio() == null || tramo.getFechaHoraFin() == null) {
                log.warn("⚠️  Tramo sin fechas reales: origen={}, destino={}. No se puede calcular estadía.",
                    tramo.getOrigen().getDireccionTextual(),
                    destino.getDireccionTextual());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede calcular costo real: tramo sin fechas reales asignadas");
            }
            
            long dias = ChronoUnit.DAYS.between(
                tramo.getFechaHoraInicio().toLocalDate(),
                tramo.getFechaHoraFin().toLocalDate()
            );
            
            // Si es el mismo día, contar como 1 día mínimo
            if (dias == 0) {
                dias = 1;
            }
            
            BigDecimal costoEstadiaTramo = deposito.getCostoEstadiaDiario()
                .multiply(new BigDecimal(dias));
            
            costoTotal = costoTotal.add(costoEstadiaTramo);
            
            log.info("  💰 Depósito '{}': {} días × ${} = ${}", 
                deposito.getNombre(),
                dias,
                deposito.getCostoEstadiaDiario(),
                costoEstadiaTramo);
        }
        
        log.info("✅ Costo total de estadía: ${}", costoTotal);
        return costoTotal;
    }
}

