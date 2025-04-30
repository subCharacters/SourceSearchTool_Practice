package com.example.sourcesearchtool_practice.contorller.advice;

import com.example.sourcesearchtool_practice.exception.NoSearchResultException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleServerError(Exception ex, Model model) {
        model.addAttribute("message", "서버 에러가 발생했습니다.");
        return "error/500";
    }

    @ExceptionHandler({NoSearchResultException.class, NoResourceFoundException.class})
    public String handleNoResult(Exception ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }
}
