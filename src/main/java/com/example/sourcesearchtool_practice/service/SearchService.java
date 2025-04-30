package com.example.sourcesearchtool_practice.service;

import com.example.sourcesearchtool_practice.dto.DocInfoDto;
import com.example.sourcesearchtool_practice.dto.RequestDto;
import com.example.sourcesearchtool_practice.dto.SearchResultDto;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {
    private final List<SearchStrategy> strategies;

    public SearchService(List<SearchStrategy> strategies) {
        this.strategies = strategies;
    }

    public Pair<List<SearchResultDto>, Pair<Long, DocInfoDto>> search(RequestDto requestDto, int maxHits) throws Exception {
        for (SearchStrategy strategy : strategies) {
            if (strategy.type(requestDto.getSearchType())) {
                return strategy.search(requestDto, maxHits);
            }
        }

        throw new Exception("지원하지 않는 검색 타입: " + requestDto.getSearchType());
    }

}
