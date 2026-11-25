package tp.backend.ms_solicitud.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tp.backend.ms_solicitud.entity.Tipo;
import tp.backend.ms_solicitud.repository.TipoRepository;

@Service
@RequiredArgsConstructor
public class TipoService {

    private final TipoRepository tipoRepository;

    public List<Tipo> findAll() {
        return tipoRepository.findAll();
    }

    public Tipo findById(Integer id) {
        return tipoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo no encontrado"));
    }

    public Tipo create(Tipo tipo) {
        if (tipo.getId() != null && tipoRepository.existsById(tipo.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tipo ya existe");
        }
        return tipoRepository.save(tipo);
    }

    public Tipo update(Integer id, Tipo tipo) {
        Tipo existente = tipoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo no encontrado"));

        if (tipo.getNombre() != null) {
            existente.setNombre(tipo.getNombre());
        }

        existente.setId(id);
        return tipoRepository.save(existente);
    }

    public void delete(Integer id) {
        if (!tipoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo no encontrado");
        }
        tipoRepository.deleteById(id);
    }
}
