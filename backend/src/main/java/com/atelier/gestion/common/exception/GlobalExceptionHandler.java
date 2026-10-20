package com.atelier.gestion.common.exception;
import com.atelier.gestion.common.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class); private static final ZoneId LIMA=ZoneId.of("America/Lima");
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex,HttpServletRequest req){Map<String,String> f=new LinkedHashMap<>();for(FieldError e:ex.getBindingResult().getFieldErrors())f.putIfAbsent(e.getField(),e.getDefaultMessage());return response(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","La solicitud contiene datos inválidos",req,f);}
  @ExceptionHandler(ConflictoException.class) ResponseEntity<ApiError> conflict(ConflictoException ex,HttpServletRequest req){return response(HttpStatus.CONFLICT,"CONFLICT",ex.getMessage(),req,Map.of());}
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ApiError> integrity(DataIntegrityViolationException ex,HttpServletRequest req){log.warn("Conflicto de integridad de datos: {}",ex.getMostSpecificCause().getMessage());return response(HttpStatus.CONFLICT,"DATA_CONFLICT","El correo, documento o registro relacionado ya existe",req,Map.of());}
  @ExceptionHandler(RecursoNoEncontradoException.class) ResponseEntity<ApiError> notFound(RecursoNoEncontradoException ex,HttpServletRequest req){return response(HttpStatus.NOT_FOUND,"NOT_FOUND",ex.getMessage(),req,Map.of());}
  @ExceptionHandler(NoAutorizadoException.class) ResponseEntity<ApiError> unauthorized(NoAutorizadoException ex,HttpServletRequest req){return response(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED",ex.getMessage(),req,Map.of());}
  @ExceptionHandler(ReglaNegocioException.class) ResponseEntity<ApiError> business(ReglaNegocioException ex,HttpServletRequest req){return response(HttpStatus.UNPROCESSABLE_ENTITY,"BUSINESS_RULE",ex.getMessage(),req,Map.of());}
  @ExceptionHandler(ArchivoInvalidoException.class) ResponseEntity<ApiError> invalidFile(ArchivoInvalidoException ex,HttpServletRequest req){return response(HttpStatus.BAD_REQUEST,"INVALID_FILE",ex.getMessage(),req,Map.of());}
  @ExceptionHandler(MaxUploadSizeExceededException.class) ResponseEntity<ApiError> maxUpload(MaxUploadSizeExceededException ex,HttpServletRequest req){return response(HttpStatus.BAD_REQUEST,"INVALID_FILE","El archivo supera el tamaño máximo permitido",req,Map.of());}
  @ExceptionHandler({ConstraintViolationException.class,MethodArgumentTypeMismatchException.class,MissingServletRequestParameterException.class}) ResponseEntity<ApiError> invalidParameter(Exception ex,HttpServletRequest req){return response(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","La solicitud contiene parámetros inválidos",req,Map.of());}
  @ExceptionHandler(AccessDeniedException.class) ResponseEntity<ApiError> forbidden(AccessDeniedException ex,HttpServletRequest req){return response(HttpStatus.FORBIDDEN,"ACCESS_DENIED","No tiene permisos para realizar esta operación",req,Map.of());}
  @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unexpected(Exception ex,HttpServletRequest req){log.error("Error no controlado",ex);return response(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL_ERROR","Ocurrió un error interno",req,Map.of());}
  private ResponseEntity<ApiError> response(HttpStatus s,String c,String m,HttpServletRequest r,Map<String,String> f){return ResponseEntity.status(s).body(new ApiError(OffsetDateTime.now(LIMA),s.value(),c,m,r.getRequestURI(),MDC.get("correlationId"),f));}
}

