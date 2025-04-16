package product.management.electronic.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import product.management.electronic.response.ErrorResponse;

import static product.management.electronic.constants.MessageConstant.ACCESS_DENIED;

@ControllerAdvice
@Component
public class GlobalExceptionHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception e, HttpStatus status, String error, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), status.value(), error, e.getMessage(), request.getDescription(false));
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, WebRequest request) {
        return buildErrorResponse(e, HttpStatus.NOT_IMPLEMENTED, "Not Implemented", request);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e, WebRequest request) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, "Bad Request", request);
    }

    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(ChangeSetPersister.NotFoundException e, WebRequest request) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, "Not Found", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, WebRequest request) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, "Bad Request", request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e, WebRequest request) {
        return buildErrorResponse(e, HttpStatus.FORBIDDEN, "Forbidden", request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Unauthorized", request);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, "Forbidden", request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, "Conflict", request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        return buildErrorResponse(e, HttpStatus.NOT_FOUND, "Resource Not Found", request);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, WebRequest request) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST, "Bad Request", request);
    }
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", HttpStatus.FORBIDDEN.value());
        errorDetails.put("error", "FORBIDDEN");
        errorDetails.put("message", ACCESS_DENIED);
        errorDetails.put("path", request.getRequestURI());

        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(errorDetails));
            writer.flush();
        }
    }
}
