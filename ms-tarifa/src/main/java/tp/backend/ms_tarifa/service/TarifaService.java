package tp.backend.ms_tarifa.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tp.backend.ms_tarifa.entity.Tarifa;
import tp.backend.ms_tarifa.repository.TarifaRepository;

@Service
@RequiredArgsConstructor
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    public List<Tarifa> findAll() {
        return tarifaRepository.findAll();
    }

    public Tarifa findById(Integer id) {
        return tarifaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarifa no encontrada"));
    }

    /**
     * Obtiene la tarifa vigente para la fecha actual.
     * @return La tarifa vigente
     * @throws ResponseStatusException si no hay tarifa vigente
     */
    public Tarifa findTarifaVigente() {
        LocalDate hoy = LocalDate.now();
        return tarifaRepository.findTarifaVigente(hoy)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "No hay tarifa vigente para la fecha actual: " + hoy));
    }

    public Tarifa create(Tarifa tarifa) {
        if (tarifa.getIdTarifa() != null && tarifaRepository.existsById(tarifa.getIdTarifa())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tarifa con ese id ya existe");
        }
        
        // Validar que las fechas sean correctas
        if (tarifa.getFechaVigenciaDesde() == null || tarifa.getFechaVigenciaHasta() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Las fechas de vigencia (desde y hasta) son obligatorias");
        }
        
        if (tarifa.getFechaVigenciaDesde().isAfter(tarifa.getFechaVigenciaHasta())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La fecha de vigencia 'desde' no puede ser posterior a la fecha 'hasta'");
        }
        
        // Validar que no se solape con otras tarifas
        if (tarifaRepository.existeSolapamiento(
                tarifa.getFechaVigenciaDesde(), 
                tarifa.getFechaVigenciaHasta(), 
                null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe una tarifa vigente para el rango de fechas especificado. " +
                "No se permiten tarifas con rangos de fechas solapados.");
        }
        
        return tarifaRepository.save(tarifa);
    }

    public Tarifa update(Integer id, Tarifa tarifa) {
        Tarifa existente = tarifaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarifa no encontrada"));

        // Actualizar campos si se proporcionan
        if (tarifa.getValorLitroCombustible() != null) {
            existente.setValorLitroCombustible(tarifa.getValorLitroCombustible());
        }
        if (tarifa.getCargoGestionPorTramo() != null) {
            existente.setCargoGestionPorTramo(tarifa.getCargoGestionPorTramo());
        }
        if (tarifa.getFechaVigenciaDesde() != null) {
            existente.setFechaVigenciaDesde(tarifa.getFechaVigenciaDesde());
        }
        if (tarifa.getFechaVigenciaHasta() != null) {
            existente.setFechaVigenciaHasta(tarifa.getFechaVigenciaHasta());
        }

        // Validar que las fechas sean correctas
        if (existente.getFechaVigenciaDesde().isAfter(existente.getFechaVigenciaHasta())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La fecha de vigencia 'desde' no puede ser posterior a la fecha 'hasta'");
        }

        // Validar que no se solape con otras tarifas (excluyendo esta misma)
        if (tarifaRepository.existeSolapamiento(
                existente.getFechaVigenciaDesde(), 
                existente.getFechaVigenciaHasta(), 
                id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Ya existe otra tarifa vigente para el rango de fechas especificado. " +
                "No se permiten tarifas con rangos de fechas solapados.");
        }

        existente.setIdTarifa(id);
        return tarifaRepository.save(existente);
    }

    public void delete(Integer id) {
        if (!tarifaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarifa no encontrada");
        }
        tarifaRepository.deleteById(id);
    }
}
