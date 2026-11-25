package tp.backend.ms_contenedor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import tp.backend.ms_contenedor.entity.Deposito;
import tp.backend.ms_contenedor.repository.DepositoRepository;

@Service
public class DepositoService {

    private final DepositoRepository depositoRepository;

    public DepositoService(DepositoRepository depositoRepository) {
        this.depositoRepository = depositoRepository;
    }

    public Deposito crear(Deposito deposito) {
        return depositoRepository.save(deposito);
    }

    public Deposito obtener(Long id) {
        return depositoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deposito no encontrado con id: " + id));
    }

    public List<Deposito> listar() {
        return depositoRepository.findAll();
    }

    public void eliminar(Long id) {
        Deposito d = obtener(id);
        depositoRepository.delete(d);
    }
}
