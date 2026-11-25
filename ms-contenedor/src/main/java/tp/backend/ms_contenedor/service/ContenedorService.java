package tp.backend.ms_contenedor.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import tp.backend.ms_contenedor.entity.Contenedor;
import tp.backend.ms_contenedor.repository.ContenedorRepository;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContenedorService {

  private final ContenedorRepository repository;

  public Contenedor crear(Contenedor contenedor) {
    return repository.save(contenedor);
  }

  public Contenedor obtener(Integer id) {
    return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));
  }

  public List<Contenedor> listar() {
    return repository.findAll();
  }

  public void eliminar(Integer id) {
    Contenedor c = obtener(id);
    repository.delete(c);
  }
}
