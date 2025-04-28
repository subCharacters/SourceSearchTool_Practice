package com.example.sourcesearchtool_practice.contorller;

import com.example.sourcesearchtool_practice.dto.DocInfoDto;
import com.example.sourcesearchtool_practice.dto.RequestDto;
import com.example.sourcesearchtool_practice.dto.ResponseDto;
import com.example.sourcesearchtool_practice.dto.SearchResultDto;
import com.example.sourcesearchtool_practice.service.LuceneSimpleSearcherService;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
        // searcher.search(null, keyword, null, null, null, maxHits);
        return new ArrayList<SearchResultDto>();
    }

    @PostMapping("/search")
    public ResponseEntity search(
            @RequestBody RequestDto requestDto
    ) throws Exception {
        int maxHits = 100;

        Pair<List<SearchResultDto>, Pair<Long, DocInfoDto>> searchResult =
                searcher.search(
                        requestDto.getRepositoryNames(),
                        requestDto.getSearchWord(),
                        requestDto.getCaseSensitive(),
                        requestDto.getSearchType(),
                        null,
                        requestDto.getLastScoreDocId(),
                        requestDto.getDocScore(),
                        maxHits);

        long totalCnt = searchResult.getSecond().getFirst();
        DocInfoDto docInfoDto = searchResult.getSecond().getSecond();
        return ResponseDto.success(searchResult.getFirst(), totalCnt, docInfoDto);
    }

    @PostMapping("/search/next")
    public ResponseEntity next(
            @RequestBody RequestDto requestDto
    ) throws Exception {
        int maxHits = 100;

        Pair<List<SearchResultDto>, Pair<Long, DocInfoDto>> searchResult =
                searcher.search(
                        requestDto.getRepositoryNames(),
                        requestDto.getSearchWord(),
                        requestDto.getCaseSensitive(),
                        requestDto.getSearchType(),
                        null,
                        requestDto.getLastScoreDocId(),
                        requestDto.getDocScore(),
                        maxHits);
        long totalCnt = searchResult.getSecond().getFirst();
        DocInfoDto docInfoDto = searchResult.getSecond().getSecond();
        return ResponseDto.success(searchResult.getFirst(), totalCnt, docInfoDto);
    }
}
