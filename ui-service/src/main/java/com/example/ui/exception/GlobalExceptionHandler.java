package com.example.ui.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public String handleWebClientResponseException(WebClientResponseException ex, Model model) {
        model.addAttribute("error", "Erro ao comunicar com o serviço: " + ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("error", "Ocorreu um erro: " + ex.getMessage());
        return "error";
    }
} 