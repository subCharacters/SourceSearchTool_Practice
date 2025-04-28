package com.example.sourcesearchtool_practice.contorller.advice;

import com.example.sourcesearchtool_practice.dto.DocInfoDto;
import com.example.sourcesearchtool_practice.dto.ErrorDto;
import com.example.sourcesearchtool_practice.dto.ResponseDto;
import com.example.sourcesearchtool_practice.exception.NoSearchResultException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSearchResultException.class)
    public ResponseEntity<ResponseDto> handleNoSearchResult(NoSearchResultException ex) {
        ErrorDto errorDto = new ErrorDto("search", ex.getMessage());
        return ResponseDto.fail(HttpStatus.NOT_FOUND, null, errorDto, 0, new DocInfoDto(-1, Float.NaN));
    }
}
