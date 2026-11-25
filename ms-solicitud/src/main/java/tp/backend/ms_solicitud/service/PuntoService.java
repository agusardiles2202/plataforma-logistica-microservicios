package tp.backend.ms_solicitud.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tp.backend.ms_solicitud.entity.Punto;
import tp.backend.ms_solicitud.repository.PuntoRepository;

@Service
@RequiredArgsConstructor
public class PuntoService {

    private final PuntoRepository puntoRepository;

    public List<Punto> findAll() {
        return puntoRepository.findAll();
    }

    public Punto findById(Integer id) {
        return puntoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Punto no encontrado"));
    }

    public Punto create(Punto punto) {
        if (punto.getId() != null && puntoRepository.existsById(punto.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Punto ya existe");
        }
        return puntoRepository.save(punto);
    }

    public Punto update(Integer id, Punto punto) {
        Punto existente = puntoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Punto no encontrado"));

        if (punto.getDireccionTextual() != null) {
            existente.setDireccionTextual(punto.getDireccionTextual());
        }
        if (punto.getLatitud() != null) {
            existente.setLatitud(punto.getLatitud());
        }
        if (punto.getLongitud() != null) {
            existente.setLongitud(punto.getLongitud());
        }
        if (punto.getDepositoId() != null) {
            existente.setDepositoId(punto.getDepositoId());
        }

        existente.setId(id);
        return puntoRepository.save(existente);
    }

    public void delete(Integer id) {
        if (!puntoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Punto no encontrado");
        }
        puntoRepository.deleteById(id);
    }
}
