package com.example.sourcesearchtool_practice.service;

import com.example.sourcesearchtool_practice.dto.DocInfoDto;
import com.example.sourcesearchtool_practice.dto.RequestDto;
import com.example.sourcesearchtool_practice.dto.SearchResultDto;
import org.springframework.data.util.Pair;

import java.util.List;

public interface SearchStrategy {
    /** 이 전략이 처리 가능한 검색 타입(line/file)인지 여부 */
    boolean type(String searchType);

    /**
     * 실제 검색 수행
     * @param requestDto 검색 요청 DTO (searchType, keyword, caseSensitive, ...)
     * @return 검색 결과 목록과 총 건수+마지막 DocInfo
     */
    Pair<List<SearchResultDto>, Pair<Long, DocInfoDto>> search(RequestDto requestDto, int maxHits) throws Exception;
}
