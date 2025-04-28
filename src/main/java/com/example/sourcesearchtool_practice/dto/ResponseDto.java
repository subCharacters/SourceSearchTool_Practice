package com.example.sourcesearchtool_practice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto<T> {
    private boolean success;
    private T data;
    private ErrorDto errorDto;
    private long totalCnt;
    private DocInfoDto docInfoDto;

    public static <T> ResponseEntity<ResponseDto> success(T data, long totalCnt, DocInfoDto docInfoDto) {
        return ResponseEntity.ok().body(new ResponseDto(true, data, null, totalCnt, docInfoDto));
    }

    public static <T> ResponseEntity<ResponseDto> fail(HttpStatus httpStatus, T data, ErrorDto errorDto, long totalCnt, DocInfoDto docInfoDto) {
        return ResponseEntity.status(httpStatus).body(new ResponseDto(false, data, errorDto, totalCnt, docInfoDto));
    }
}
