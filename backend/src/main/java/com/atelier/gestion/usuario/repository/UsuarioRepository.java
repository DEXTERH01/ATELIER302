package com.atelier.gestion.usuario.repository;
import com.atelier.gestion.usuario.entity.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UsuarioRepository extends JpaRepository<Usuario,UUID>{
  boolean existsByCorreoIgnoreCase(String correo);
  @EntityGraph(attributePaths="rol") Optional<Usuario> findByCorreoIgnoreCase(String correo);
}

