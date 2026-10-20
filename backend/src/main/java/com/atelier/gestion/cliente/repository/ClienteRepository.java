package com.atelier.gestion.cliente.repository;
import com.atelier.gestion.cliente.entity.Cliente;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ClienteRepository extends JpaRepository<Cliente,UUID>{boolean existsByNumeroDocumento(String numeroDocumento);}

