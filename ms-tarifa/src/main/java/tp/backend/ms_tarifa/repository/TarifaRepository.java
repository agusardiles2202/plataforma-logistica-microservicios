package tp.backend.ms_tarifa.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tp.backend.ms_tarifa.entity.Tarifa;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    /**
     * Busca la tarifa vigente para una fecha específica.
     * Una tarifa está vigente si la fecha está entre fechaVigenciaDesde y fechaVigenciaHasta.
     */
    @Query("SELECT t FROM Tarifa t WHERE :fecha BETWEEN t.fechaVigenciaDesde AND t.fechaVigenciaHasta")
    Optional<Tarifa> findTarifaVigente(@Param("fecha") LocalDate fecha);

    /**
    
     * Dos rangos se solapan si:
     * - El inicio del nuevo rango está dentro de un rango existente, O
     * - El fin del nuevo rango está dentro de un rango existente, O
     * - El nuevo rango contiene completamente un rango existente
     */
    @Query("SELECT COUNT(t) > 0 FROM Tarifa t WHERE " +
           "(:idExcluir IS NULL OR t.idTarifa <> :idExcluir) AND " +
           "((:desde BETWEEN t.fechaVigenciaDesde AND t.fechaVigenciaHasta) OR " +
           " (:hasta BETWEEN t.fechaVigenciaDesde AND t.fechaVigenciaHasta) OR " +
           " (t.fechaVigenciaDesde BETWEEN :desde AND :hasta))")
    boolean existeSolapamiento(
        @Param("desde") LocalDate desde, 
        @Param("hasta") LocalDate hasta,
        @Param("idExcluir") Integer idExcluir
    );
}

