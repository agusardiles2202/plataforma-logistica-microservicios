package tp.backend.ms_contenedor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tp.backend.ms_contenedor.entity.Contenedor;
import org.springframework.stereotype.Repository;
@Repository

public interface ContenedorRepository extends JpaRepository<Contenedor, Integer> {} //el contenedor es la entidad con la que vamos a trabajar y el Integer es el tipo de dato del id
//Jpa repository nos brinda los mtodos CRUD predefinidos para interactuar con la base de datos
//Nos lo brinda spring data jpa, que es una dependencia que agregamos en el pom.xml