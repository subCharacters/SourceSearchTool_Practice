package com.example.sourcesearchtool_practice.contorller;

import com.example.sourcesearchtool_practice.dto.RequestDto;
import com.example.sourcesearchtool_practice.dto.SearchResultDto;
import com.example.sourcesearchtool_practice.service.LuceneSimpleSearcherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SearchController {
    private final LuceneSimpleSearcherService searcher;

    public SearchController(LuceneSimpleSearcherService searcher) {
        this.searcher = searcher;
    }

    @GetMapping("/search")
    public List<SearchResultDto> search(
            @RequestParam String project,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int maxHits
    ) throws Exception {
        project = "DesignPattern_Java";
        return searcher.search(project, keyword, maxHits);
    }

    @PostMapping("/search")
    public List<SearchResultDto> search(
            @RequestBody RequestDto requestDto
    ) throws Exception {
        String project = "DesignPattern_Java";
        String keyword = "Java";
        int maxHits = 10;
        return searcher.search(project, requestDto.getKeyword(), maxHits);
    }
}
