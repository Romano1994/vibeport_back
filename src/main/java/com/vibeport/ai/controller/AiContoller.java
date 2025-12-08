package com.vibeport.ai.controller;

import com.vibeport.ai.service.LangChainService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/vibeport/ai/")
@RequiredArgsConstructor
public class AiContoller {

    private final LangChainService aiService;

    @GetMapping("getConcertInfo")
    public void updateConcertInfo(HttpServletRequest request, HttpServletResponse response) throws Exception{
        this.aiService.fetchAndNotifyNewConcerts();
    }

}
