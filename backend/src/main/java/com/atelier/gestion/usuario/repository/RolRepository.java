package com.atelier.gestion.usuario.repository;
import com.atelier.gestion.usuario.entity.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RolRepository extends JpaRepository<Rol,Short>{Optional<Rol> findByNombre(String nombre);}

