package tp.backend.ms_solicitud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tp.backend.ms_solicitud.entity.Ruta;
import tp.backend.ms_solicitud.entity.Tramo;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Integer> {

	// Buscar tramos por dominio de camión (dominio es la PK del Camion)
	List<Tramo> findByCamionDominio(String dominio);
	
	// Buscar tramos por ID de ruta
	List<Tramo> findByRutaId(Integer rutaId);
	
	// Eliminar todos los tramos de una ruta específica
	void deleteByRuta(Ruta ruta);
}
