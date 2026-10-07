package uz.agrobank.stopcredit.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

// Framework exceptions handled by the parent class get their Uzbek detail from messages.properties
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApi(ApiException ex) {
        return ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleInvalidSort(PropertyReferenceException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Noma'lum maydon: " + ex.getPropertyName());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrityViolation(DataIntegrityViolationException ex) {
        String constraint = ex.getCause() instanceof ConstraintViolationException cause
                ? cause.getConstraintName() : "unknown";
        log.warn("Data integrity violation: constraint={}", constraint);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Ma'lumot mavjud yozuv bilan to'qnashdi. Sahifani yangilab, qayta urinib ko'ring");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.info("Optimistic lock conflict: {}", ex.getPersistentClassName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Yozuvni boshqa foydalanuvchi o'zgartirgan. Sahifani yangilab, qayta urinib ko'ring");
    }

    @ExceptionHandler(StorageException.class)
    public ProblemDetail handleStorage(StorageException ex) {
        log.error("File storage failure", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Fayl ombori vaqtincha ishlamayapti. Keyinroq qayta urinib ko'ring");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Kutilmagan server xatosi. Keyinroq qayta urinib ko'ring");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.putIfAbsent(e.getField(), messageOf(e)));
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Ma'lumotlar noto'g'ri to'ldirilgan");
        body.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    // a value that could not even be converted (e.g. status=FOO) carries Spring's English conversion text
    private static String messageOf(FieldError error) {
        return error.isBindingFailure() ? "qiymati noto'g'ri: " + error.getRejectedValue() : error.getDefaultMessage();
    }
}
