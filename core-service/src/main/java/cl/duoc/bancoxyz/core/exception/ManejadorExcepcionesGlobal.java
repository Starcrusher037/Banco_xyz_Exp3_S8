package cl.duoc.bancoxyz.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Interceptor global de excepciones para el microservicio core-service.
 * Estandariza las respuestas de error en formato ErrorRespuestaDto,
 * asegurando consistencia y proteccion de detalles de implementacion internos.
 */
@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    /**
     * Captura excepciones de recursos no encontrados en la base de datos o repositorios.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorRespuestaDto> handleNotFound(NoSuchElementException ex, WebRequest request) {
        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("NOT_FOUND")
                .mensaje(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Captura violaciones de reglas de negocio o argumentos no validos pasados a metodos.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuestaDto> handleBadRequest(IllegalArgumentException ex, WebRequest request) {
        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("BAD_REQUEST")
                .mensaje(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Captura errores de validacion de datos de entrada (@Valid en RequestBody),
     * procesando los campos rechazados en el BindingResult y retornando un mensaje estructurado.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuestaDto> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String detalleValidaciones = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .mensaje(detalleValidaciones)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Capturador de respaldo para cualquier excepcion imprevista del servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDto> handleGlobalException(Exception ex, WebRequest request) {
        ErrorRespuestaDto error = ErrorRespuestaDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .mensaje("Ocurrio un error inesperado al procesar la operacion: " + ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
