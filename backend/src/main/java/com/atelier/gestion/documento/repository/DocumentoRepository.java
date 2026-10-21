package com.atelier.gestion.documento.repository;
import com.atelier.gestion.documento.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface DocumentoRepository extends JpaRepository<Documento,UUID>{
  @EntityGraph(attributePaths={"expediente","expediente.proyecto","expediente.proyecto.cliente","expediente.proyecto.cliente.usuario","subidoPor","estudio"}) List<Documento> findByExpedienteIdAndVigenteTrueOrderByCreadoEnDesc(UUID expedienteId);
  @EntityGraph(attributePaths={"expediente","expediente.proyecto","expediente.proyecto.cliente","expediente.proyecto.cliente.usuario","subidoPor","estudio"}) List<Documento> findByExpedienteIdAndVigenteTrueAndVisibleClienteTrueOrderByCreadoEnDesc(UUID expedienteId);
  @Override @EntityGraph(attributePaths={"expediente","expediente.proyecto","expediente.proyecto.cliente","expediente.proyecto.cliente.usuario","subidoPor","estudio"}) Optional<Documento> findById(UUID id);
  @Query("select coalesce(max(d.version),0) from Documento d where d.expediente.id=:expedienteId and d.tipoDocumento=:tipo and d.categoria=:categoria and ((:estudioId is null and d.estudio is null) or d.estudio.id=:estudioId)") short maxVersion(@Param("expedienteId")UUID expedienteId,@Param("tipo")String tipo,@Param("categoria")CategoriaDocumento categoria,@Param("estudioId")UUID estudioId);
  @Query("select d from Documento d where d.expediente.id=:expedienteId and d.tipoDocumento=:tipo and d.categoria=:categoria and d.vigente=true and ((:estudioId is null and d.estudio is null) or d.estudio.id=:estudioId)") List<Documento> vigentesDeSerie(@Param("expedienteId")UUID expedienteId,@Param("tipo")String tipo,@Param("categoria")CategoriaDocumento categoria,@Param("estudioId")UUID estudioId);
  boolean existsByEstudioIdAndVigenteTrue(UUID estudioId);
}
