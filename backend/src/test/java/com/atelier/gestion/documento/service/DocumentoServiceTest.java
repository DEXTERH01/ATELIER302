package com.atelier.gestion.documento.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.atelier.gestion.auditoria.service.AuditoriaService;
import com.atelier.gestion.common.exception.ReglaNegocioException;
import com.atelier.gestion.common.storage.*;
import com.atelier.gestion.config.AtelierProperties;
import com.atelier.gestion.documento.entity.*;
import com.atelier.gestion.documento.repository.DocumentoRepository;
import com.atelier.gestion.estudio.repository.EstudioTecnicoRepository;
import com.atelier.gestion.expediente.entity.Expediente;
import com.atelier.gestion.expediente.repository.ExpedienteRepository;
import com.atelier.gestion.proyecto.entity.Proyecto;
import com.atelier.gestion.proyecto.repository.ProyectoRepository;
import com.atelier.gestion.proyecto.service.AutorizacionProyectoService;
import com.atelier.gestion.usuario.entity.Usuario;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.*;

class DocumentoServiceTest {
  private final DocumentoRepository documentos=mock(DocumentoRepository.class);
  private final ExpedienteRepository expedientes=mock(ExpedienteRepository.class);
  private final EstudioTecnicoRepository estudios=mock(EstudioTecnicoRepository.class);
  private final ProyectoRepository proyectos=mock(ProyectoRepository.class);
  private final AutorizacionProyectoService acceso=mock(AutorizacionProyectoService.class);
  private final ArchivoStorage storage=mock(ArchivoStorage.class);
  private final ValidadorArchivo validador=mock(ValidadorArchivo.class);
  private final AuditoriaService auditoria=mock(AuditoriaService.class);
  private DocumentoService service;
  private Proyecto proyecto;
  private Expediente expediente;
  private Usuario usuario;
  private UUID proyectoId;
  private UUID expedienteId;

  @BeforeEach void setUp(){
    proyecto=mock(Proyecto.class);expediente=mock(Expediente.class);usuario=mock(Usuario.class);
    proyectoId=UUID.randomUUID();expedienteId=UUID.randomUUID();
    when(proyecto.getId()).thenReturn(proyectoId);when(proyecto.getCodigo()).thenReturn("PROY-2026-0001");
    when(expediente.getId()).thenReturn(expedienteId);when(expediente.getProyecto()).thenReturn(proyecto);
    when(acceso.usuario()).thenReturn(usuario);when(acceso.esGestor(usuario)).thenReturn(true);
    service=new DocumentoService(documentos,expedientes,estudios,proyectos,acceso,storage,validador,
        propiedades(),auditoria,Clock.fixed(Instant.parse("2026-10-20T17:00:00Z"),ZoneOffset.UTC));
  }

  @Test void conservaVigenteCuandoCloudinaryNoConfirmaEliminacion(){
    UUID id=UUID.randomUUID();Documento documento=documento(id);
    when(documentos.findById(id)).thenReturn(Optional.of(documento));
    when(storage.eliminar("atelier/test/informe","raw")).thenReturn(false);

    assertThatThrownBy(()->service.eliminar(id)).isInstanceOf(ReglaNegocioException.class)
        .hasMessageContaining("se conserva vigente");
    assertThat(documento.isVigente()).isTrue();
    verifyNoInteractions(auditoria);
  }

  @Test void retiraDocumentoSoloDespuesDeConfirmarCloudinary(){
    UUID id=UUID.randomUUID();Documento documento=documento(id);
    when(documentos.findById(id)).thenReturn(Optional.of(documento));
    when(storage.eliminar("atelier/test/informe","raw")).thenReturn(true);

    service.eliminar(id);

    assertThat(documento.isVigente()).isFalse();
    verify(auditoria).registrar(eq(usuario),eq("documentos"),eq(id),eq("DELETE"),anyMap());
  }

  @Test void rollbackDeBaseDeDatosCompensaArchivoRecienSubido(){
    when(expedientes.bloquear(expedienteId)).thenReturn(Optional.of(expediente));
    ArchivoAlmacenado remoto=new ArchivoAlmacenado("https://cloudinary/informe.pdf",
        "atelier/test/informe","raw","application/pdf",128);
    when(storage.subir(any(),anyString())).thenReturn(remoto);
    when(documentos.maxVersion(expedienteId,"INFORME",CategoriaDocumento.EXPEDIENTE,null)).thenReturn((short)0);
    when(documentos.vigentesDeSerie(expedienteId,"INFORME",CategoriaDocumento.EXPEDIENTE,null)).thenReturn(List.of());
    when(documentos.save(any())).thenAnswer(invocation->{
      Documento guardado=invocation.getArgument(0);ReflectionTestUtils.setField(guardado,"id",UUID.randomUUID());return guardado;
    });
    TransactionSynchronizationManager.initSynchronization();
    try{
      service.subir(expedienteId,"INFORME",CategoriaDocumento.EXPEDIENTE,null,false,
          new MockMultipartFile("archivo","informe.pdf","application/pdf",new byte[]{1,2}));
      TransactionSynchronizationManager.getSynchronizations().forEach(s->
          s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
      verify(storage).eliminar("atelier/test/informe","raw");
    }finally{TransactionSynchronizationManager.clearSynchronization();}
  }

  private Documento documento(UUID id){
    Documento d=new Documento(expediente,usuario,null,"informe.pdf","INFORME",(short)1,
        "https://cloudinary/informe.pdf","atelier/test/informe","pdf","application/pdf",128,
        CategoriaDocumento.EXPEDIENTE,"raw",false,OffsetDateTime.now(ZoneOffset.UTC));
    ReflectionTestUtils.setField(d,"id",id);return d;
  }

  private AtelierProperties propiedades(){return new AtelierProperties(new AtelierProperties.Cors("http://localhost"),
      new AtelierProperties.Jwt("01234567890123456789012345678901","test",15,7),
      new AtelierProperties.Cookie("REFRESH",false),new AtelierProperties.Cloudinary("demo","key","secret"),
      new AtelierProperties.Files(10_485_760),"test");}
}
