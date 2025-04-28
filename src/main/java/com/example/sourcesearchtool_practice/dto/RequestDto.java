package com.example.sourcesearchtool_practice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {
    /**
     * 검색 키워드
     */
    private String searchWord;
    
    /**
     * 확장자
     */
    private String fileExtension;

    /**
     * 검색 옵션
     * - 대소문자 구분
     * - 대소문자 구분없음
     */
    private String caseSensitive;

    /**
     * 검색 유형
     * - 행 기준 검색
     * - 파일 기준 검색
     */
    private String searchType;

    /**
     * 결과내 재검색 -> 불필요?
     */
    private boolean searchWithinResults;

    /**
     * 리포지토리명
     */
    private List<String> repositoryNames;

    /**
     * Doc Id
     */
    private int lastScoreDocId;

    /**
     * docScore
     */
    private float docScore;
}
