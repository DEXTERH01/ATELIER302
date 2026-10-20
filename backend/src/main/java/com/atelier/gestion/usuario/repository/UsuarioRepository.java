package com.atelier.gestion.usuario.repository;
import com.atelier.gestion.usuario.entity.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface UsuarioRepository extends JpaRepository<Usuario,UUID>{
  boolean existsByCorreoIgnoreCase(String correo);
  @EntityGraph(attributePaths="rol") Optional<Usuario> findByCorreoIgnoreCase(String correo);
  @Override @EntityGraph(attributePaths="rol") Optional<Usuario> findById(UUID id);
  @EntityGraph(attributePaths="rol") Page<Usuario> findByCorreoContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(String correo,String nombres,String apellidos,Pageable pageable);
}

