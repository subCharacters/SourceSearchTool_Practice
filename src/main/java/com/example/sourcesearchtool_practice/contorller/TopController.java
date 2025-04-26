package com.example.sourcesearchtool_practice.contorller;

import com.example.sourcesearchtool_practice.dto.TopContentDto;
import com.example.sourcesearchtool_practice.service.TopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopController {
    private final TopService topService;

    public TopController(TopService topService) {
        this.topService = topService;
    }

    @GetMapping("/top")
    public String top(Model model) {
        TopContentDto topContents = topService.getTopRepoContents();
        model.addAttribute("topContents", topContents);
        return "top";
    }
}
