package com.atelier.gestion.cliente.repository;
import com.atelier.gestion.cliente.entity.Cliente;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ClienteRepository extends JpaRepository<Cliente,UUID>{
  boolean existsByNumeroDocumento(String numeroDocumento);
  boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento,UUID id);
  @EntityGraph(attributePaths={"usuario","usuario.rol"}) Optional<Cliente> findByUsuarioCorreoIgnoreCase(String correo);
  @Override @EntityGraph(attributePaths={"usuario","usuario.rol"}) Optional<Cliente> findById(UUID id);
  @EntityGraph(attributePaths={"usuario","usuario.rol"}) Page<Cliente> findByNumeroDocumentoContainingIgnoreCaseOrUsuarioCorreoContainingIgnoreCase(String documento,String correo,Pageable pageable);
}

