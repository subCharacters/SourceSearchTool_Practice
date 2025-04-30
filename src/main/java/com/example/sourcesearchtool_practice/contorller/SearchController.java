package com.example.sourcesearchtool_practice.contorller;

import com.example.sourcesearchtool_practice.dto.*;
import com.example.sourcesearchtool_practice.exception.NoSearchResultException;
import com.example.sourcesearchtool_practice.service.SearchService;
import com.example.sourcesearchtool_practice.service.legacy.LuceneSimpleSearcherService;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SearchController {
    private final LuceneSimpleSearcherService searcher;
    private final SearchService searchService;

    public SearchController(LuceneSimpleSearcherService searcher, SearchService searchService) {
        this.searcher = searcher;
        this.searchService = searchService;
    }

    @PostMapping("/search2")
    public ResponseEntity search2(
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

    @PostMapping("/search/next2")
    public ResponseEntity next2(
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

    @PostMapping("/search")
    public ResponseEntity search(@RequestBody RequestDto requestDto) throws Exception {
        int maxHits = 100;
        // TODO search Keyword는 controller에서 체크.
        // TODO next는 url패스를 보고 분기로 해도 될 듯?.
        try {
            Pair<List<SearchResultDto>, Pair<Long, DocInfoDto>> searchResult = searchService.search(requestDto, maxHits);
            long totalCnt = searchResult.getSecond().getFirst();
            DocInfoDto docInfoDto = searchResult.getSecond().getSecond();
            return ResponseDto.success(searchResult.getFirst(), totalCnt, docInfoDto);
        } catch (NoSearchResultException e) {
            ErrorDto errorDto = new ErrorDto("search", e.getMessage());
            return ResponseDto.fail(HttpStatus.NOT_FOUND, null, errorDto, 0, new DocInfoDto(-1, Float.NaN));
        }
    }

    @PostMapping("/search/next")
    public ResponseEntity next(@RequestBody RequestDto requestDto) throws Exception {
        int maxHits = 100;

        try {
            Pair<List<SearchResultDto>, Pair<Long, DocInfoDto>> searchResult = searchService.search(requestDto, maxHits);
            long totalCnt = searchResult.getSecond().getFirst();
            DocInfoDto docInfoDto = searchResult.getSecond().getSecond();
            return ResponseDto.success(searchResult.getFirst(), totalCnt, docInfoDto);
        } catch (NoSearchResultException e) {
            ErrorDto errorDto = new ErrorDto("search", e.getMessage());
            return ResponseDto.fail(HttpStatus.NOT_FOUND, null, errorDto, 0, new DocInfoDto(-1, Float.NaN));
        }
    }
}
