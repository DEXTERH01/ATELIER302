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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
@Component public class RestAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper mapper; public RestAccessDeniedHandler(ObjectMapper mapper){this.mapper=mapper;}
  @Override public void handle(HttpServletRequest req,HttpServletResponse res,AccessDeniedException ex)throws IOException{res.setStatus(403);res.setContentType(MediaType.APPLICATION_JSON_VALUE);mapper.writeValue(res.getOutputStream(),new ApiError(OffsetDateTime.now(ZoneId.of("America/Lima")),403,"ACCESS_DENIED","No tiene permisos para realizar esta operación",req.getRequestURI(),MDC.get("correlationId"),Map.of()));}
}

