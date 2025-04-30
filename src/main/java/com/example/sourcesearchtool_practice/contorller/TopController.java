package com.example.sourcesearchtool_practice.contorller;

import com.example.sourcesearchtool_practice.dto.TopContentDto;
import com.example.sourcesearchtool_practice.exception.NoSearchResultException;
import com.example.sourcesearchtool_practice.service.TopService;
import com.example.sourcesearchtool_practice.util.IndexingCheck;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TopController {
    private final TopService topService;
    private final IndexingCheck indexingCheck;

    public TopController(TopService topService, IndexingCheck indexingCheck) {
        this.topService = topService;
        this.indexingCheck = indexingCheck;
    }

    @GetMapping("/top")
    public String top(Model model) {
        TopContentDto topContents = topService.getTopRepoContents();
        model.addAttribute("topContents", topContents);
        return "top";
    }

    @RequestMapping("/indexingProgress")
    public String indexingProgress() {
        if (indexingCheck.isIndexing()) {
            return "indexingProgress";
        }
        return "redirect:/top";
    }

    @GetMapping("/404")
    public String notFound(Model model) throws NoSearchResultException {
        throw new NoSearchResultException("");
    }

    @GetMapping("/500")
    public String internalError(Model model) throws Exception {
        throw new Exception("");
    }

    @GetMapping("/indexing")
    public String idexing(Model model) throws Exception {
        return "indexing";
    }
}
