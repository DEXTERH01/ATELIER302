package com.atelier.gestion.security;
import com.atelier.gestion.common.api.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
@Component public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper mapper; public RestAuthenticationEntryPoint(ObjectMapper mapper){this.mapper=mapper;}
  @Override public void commence(HttpServletRequest req,HttpServletResponse res,AuthenticationException ex)throws IOException{res.setStatus(401);res.setContentType(MediaType.APPLICATION_JSON_VALUE);mapper.writeValue(res.getOutputStream(),new ApiError(OffsetDateTime.now(ZoneId.of("America/Lima")),401,"UNAUTHORIZED","Se requiere autenticación",req.getRequestURI(),MDC.get("correlationId"),Map.of()));}
}

