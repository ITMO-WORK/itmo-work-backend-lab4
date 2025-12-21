package org.ilestegor.applicationservice.exception;

import org.ilestegor.applicationservice.exception.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> userNotFoundExceptionHandler(UserNotFoundException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.NOT_FOUND, "User not found", "", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(UserHasAlreadyAppliedException.class)
    public ResponseEntity<ProblemDetail> userHasAlreadyAppliedExceptionHandler(UserHasAlreadyAppliedException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "User has already applied for current vacancy", "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UserApplicationNotFoundException.class)
    public ResponseEntity<ProblemDetail> userApplicationNotFoundExceptionHandler(UserApplicationNotFoundException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "User application not found", "", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ApplicationStatusNotFoundException.class)
    public ResponseEntity<ProblemDetail> applicationStatusNotFoundExceptionHandler(ApplicationStatusNotFoundException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Application status not found", "", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidApplicationStatusForApplicationUpdate.class)
    public ResponseEntity<ProblemDetail> invalidApplicationStatusForApplicationUpdateExceptionHandler(InvalidApplicationStatusForApplicationUpdate ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Application status is not new, cant update application", "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UserDoesNotBelongsToCompanyException.class)
    public ResponseEntity<ProblemDetail> userDoesNotBelongToCompanyExceptionHandler(UserDoesNotBelongsToCompanyException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.NOT_FOUND, "User does not belong to any company", "", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(VacancyNotFoundException.class)
    public ResponseEntity<ProblemDetail> vacancyNotFoundExceptionHandler(VacancyNotFoundException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.NOT_FOUND, "Vacancy not found", "", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(VacancyNotPublishedException.class)
    public ResponseEntity<ProblemDetail> vacancyNotPublishedExceptionHandler(VacancyNotPublishedException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Vacancy is not published", "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> companyNotPublishedExceptionHandler(BadCredentialsException ex, ServerWebExchange exchange) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.UNAUTHORIZED, "Token incorrect", ex.getMessage(), exchange);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ProblemDetail> applicationNotFoundExceptionHandler(ApplicationNotFoundException ex, ServerWebExchange exchange) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Application not found", ex.getMessage(), exchange);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<ProblemDetail> resumeNotFoundExceptionHandler(ResumeNotFoundException ex, ServerWebExchange exchange) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Resume not found", ex.getMessage(), exchange);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalJsonFormatException.class)
    public ResponseEntity<ProblemDetail> illegalJsonFormatExceptionHandler(IllegalJsonFormatException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Not valid JSON format", "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(VacancyBadRequest.class)
    public ResponseEntity<ProblemDetail> vacancyBadRequestExceptionHandler(VacancyBadRequest ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(WrongEventException.class)
    public ResponseEntity<ProblemDetail> wrongEventExceptionHandler(WrongEventException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(VacancyInternalError.class)
    public ResponseEntity<ProblemDetail> vacancyInternalErrorExceptionHandler(VacancyInternalError ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "", request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(ForbiddenErrorException.class)
    public ResponseEntity<ProblemDetail> forbiddenErrorExceptionHandler(ForbiddenErrorException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.FORBIDDEN, ex.getMessage(), "", request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> forbiddenErrorExceptionHandler(UnauthorizedException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.UNAUTHORIZED, ex.getMessage(), "", request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ProblemDetail> timeoutExceptionHandler(TimeoutException ex, ServerWebExchange request) {
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.REQUEST_TIMEOUT, ex.getMessage(), "", request);
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(body);
    }
}
