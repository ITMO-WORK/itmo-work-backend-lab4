package org.itmo.work.fileservice.adapter.input.web.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.itmo.work.fileservice.domain.exception.ApplicationNotFoundException;
import org.itmo.work.fileservice.domain.exception.IllegalJsonFormatException;
import org.itmo.work.fileservice.domain.exception.ResumeAlreadyExistsException;
import org.itmo.work.fileservice.domain.exception.ResumeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResumeAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> resumeNotFoundExceptionHandler(ResumeAlreadyExistsException ex, HttpServletRequest request){
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Resume already exists for this application", "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalJsonFormatException.class)
    public ResponseEntity<ProblemDetail> illegalJsonFormatExceptionHandler(IllegalJsonFormatException ex, HttpServletRequest request){
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Not valid JSON format", "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ProblemDetail> applicationNotFoundExceptionHandler(ApplicationNotFoundException ex, HttpServletRequest request){
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Application not found", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<ProblemDetail> resumeNotFoundExceptionHandler(ResumeNotFoundException ex, HttpServletRequest request){
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Application not found", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
