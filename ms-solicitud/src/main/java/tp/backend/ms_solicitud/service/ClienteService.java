package tp.backend.ms_solicitud.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import tp.backend.ms_solicitud.entity.Cliente;
import tp.backend.ms_solicitud.repository.ClienteRepository;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Cliente findById(Integer dni) {
        return clienteRepository.findById(dni)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    public Cliente create(Cliente cliente) {
        if (cliente.getDniCliente() != null && clienteRepository.existsById(cliente.getDniCliente())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cliente ya existe");
        }
        return clienteRepository.save(cliente);
    }

    public Cliente update(Integer dni, Cliente cliente) {
        Cliente existente = clienteRepository.findById(dni)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        if (cliente.getNombre() != null) existente.setNombre(cliente.getNombre());
        if (cliente.getApellido() != null) existente.setApellido(cliente.getApellido());
        if (cliente.getTelefono() != null) existente.setTelefono(cliente.getTelefono());

        existente.setDniCliente(dni);
        return clienteRepository.save(existente);
    }

    public void delete(Integer dni) {
        if (!clienteRepository.existsById(dni)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }
        clienteRepository.deleteById(dni);
    }

    public boolean existsById(Integer dni) {
        return clienteRepository.existsById(dni);
    }
}
