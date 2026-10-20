package com.atelier.gestion.auditoria.repository;

import com.atelier.gestion.auditoria.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {}
