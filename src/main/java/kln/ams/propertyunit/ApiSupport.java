package kln.ams.propertyunit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;

@Component
class RequestIds extends OncePerRequestFilter {
    private static final ThreadLocal<String> ID = new ThreadLocal<>();
    static String current() { String id=ID.get(); return id==null?UUID.randomUUID().toString():id; }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        String incoming=request.getHeader("X-Request-ID");
        String id=incoming!=null && incoming.length()<=128 && incoming.matches("[A-Za-z0-9._-]+") ? incoming : UUID.randomUUID().toString();
        ID.set(id); MDC.put("requestId",id); response.setHeader("X-Request-ID",id);
        try { chain.doFilter(request,response); } finally { ID.remove(); MDC.remove("requestId"); }
    }
}

class ApiException extends RuntimeException {
    final HttpStatus status; final String code; final Object details;
    ApiException(HttpStatus status,String code,String message) { this(status,code,message,null); }
    ApiException(HttpStatus status,String code,String message,Object details) { super(message);this.status=status;this.code=code;this.details=details; }
}

@RestControllerAdvice
class ApiErrors {
    private static final Logger log=LoggerFactory.getLogger(ApiErrors.class);
    @ExceptionHandler(ApiException.class) ResponseEntity<ApiError> known(ApiException e) { return ResponseEntity.status(e.status).body(ApiError.of(e.getMessage(),e.code,e.details)); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) {
        List<Map<String,String>> details=e.getBindingResult().getFieldErrors().stream().map(f->Map.of("field",f.getField(),"message",f.getDefaultMessage()==null?"Invalid value":f.getDefaultMessage())).toList();
        return ResponseEntity.badRequest().body(ApiError.of("Validation failed","VALIDATION_ERROR",details));
    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class,HttpMessageNotReadableException.class,IllegalArgumentException.class})
    ResponseEntity<ApiError> invalid(Exception e) { return ResponseEntity.badRequest().body(ApiError.of("Invalid request","VALIDATION_ERROR",null)); }
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ApiError> integrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of("Resource conflicts with existing data","BUSINESS_RULE_VIOLATION",null));
    }
    @ExceptionHandler(Exception.class) ResponseEntity<ApiError> unexpected(Exception e) {
        log.error("service=property-unit-service requestId={} errorCode=INTERNAL_SERVER_ERROR",RequestIds.current(),e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.of("Unexpected server failure","INTERNAL_SERVER_ERROR",null));
    }
}

@Component
class SecurityErrors {
    private final ObjectMapper mapper;
    SecurityErrors(ObjectMapper mapper) { this.mapper=mapper; }
    void write(HttpServletResponse response,HttpStatus status,String code,String message) throws IOException {
        response.setStatus(status.value());response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(),ApiError.of(message,code,null));
    }
}
