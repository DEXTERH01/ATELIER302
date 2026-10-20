package com.atelier.gestion.auth.dto;
import static org.assertj.core.api.Assertions.assertThat;
import com.atelier.gestion.auth.dto.AuthDtos.RegisterClientRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
class RegisterClientValidationTest {
  @Test void exigePasswordRobusta(){try(var factory=Validation.buildDefaultValidatorFactory()){var validator=factory.getValidator();var request=new RegisterClientRequest("cliente@atelier.pe","debil","Ana","Pérez",null,"NATURAL","DNI","12345678",null,null,null);assertThat(validator.validate(request)).extracting(v->v.getPropertyPath().toString()).contains("password");}}
  @Test void contratoNoExponeCampoRol(){assertThat(RegisterClientRequest.class.getRecordComponents()).extracting(java.lang.reflect.RecordComponent::getName).doesNotContain("rol");}
}

