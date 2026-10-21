package com.atelier.gestion.especialidad.repository;
import com.atelier.gestion.especialidad.entity.Especialidad;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EspecialidadRepository extends JpaRepository<Especialidad,Short>{
  List<Especialidad> findAllByOrderByNombreAsc();
  boolean existsByNombreIgnoreCase(String nombre);
  boolean existsByNombreIgnoreCaseAndIdNot(String nombre,Short id);
}
