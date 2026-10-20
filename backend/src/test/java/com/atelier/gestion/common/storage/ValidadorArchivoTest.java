package com.atelier.gestion.common.storage;

import static org.assertj.core.api.Assertions.*;

import com.atelier.gestion.common.exception.ArchivoInvalidoException;
import com.atelier.gestion.config.AtelierProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ValidadorArchivoTest {
  private final ValidadorArchivo validador=new ValidadorArchivo(new AtelierProperties(
      new AtelierProperties.Cors("http://localhost"),
      new AtelierProperties.Jwt("01234567890123456789012345678901","test",15,7),
      new AtelierProperties.Cookie("REFRESH",false),
      new AtelierProperties.Cloudinary("demo","key","secret"),new AtelierProperties.Files(10),"test"));

  @Test void aceptaPdfCuandoExtensionMimeYTamanoCoinciden(){
    assertThatCode(()->validador.validar(new MockMultipartFile("archivo","pago.pdf","application/pdf",new byte[]{1,2}))).doesNotThrowAnyException();
  }
  @Test void rechazaExtensionDisfrazada(){
    assertThatThrownBy(()->validador.validar(new MockMultipartFile("archivo","pago.pdf","image/png",new byte[]{1,2}))).isInstanceOf(ArchivoInvalidoException.class);
  }
  @Test void rechazaArchivoQueExcedeElLimite(){
    assertThatThrownBy(()->validador.validar(new MockMultipartFile("archivo","pago.png","image/png",new byte[11]))).isInstanceOf(ArchivoInvalidoException.class).hasMessageContaining("tamaño");
  }
}
