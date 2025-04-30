package com.example.sourcesearchtool_practice.service;

import com.example.sourcesearchtool_practice.dto.DocInfoDto;
import com.example.sourcesearchtool_practice.dto.RequestDto;
import com.example.sourcesearchtool_practice.dto.SearchResultDto;
import com.example.sourcesearchtool_practice.exception.NoSearchResultException;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FileSearchStrategy implements SearchStrategy {
    @Override
    public boolean type(String searchType) {
        return "file".equals(searchType);
    }

    @Override
    public Pair<List<SearchResultDto>, Pair<Long, DocInfoDto>> search(RequestDto requestDto, int maxHits) throws Exception {
        // 0) dto로부터 값 설정
        String searchKeyword = requestDto.getSearchWord();
        boolean isCaseSensitive = requestDto.getCaseSensitive().equals("case"); // 대소문자 구분인가. true : 한다, false : 안한다
        int lastScoreDocId = requestDto.getLastScoreDocId();
        float docScore = requestDto.getDocScore();

        // 1) 검색 대상 리포지토리 취득
        List<String> projectNames = requestDto.getRepositoryNames();
        if (projectNames == null || projectNames.isEmpty()) {
            projectNames = getRepositoryList();
        }

        // 2) 인덱스 폴더 열기
        List<IndexReader> readers = new ArrayList<>();
        for (String projectName : projectNames) {
            Path indexPath = Paths.get("index_file", projectName);
            Directory directory = FSDirectory.open(indexPath);
            readers.add(DirectoryReader.open(directory));
        }

        // 3) Reader를 MultiReader로 묶기 (대상 리포지토리 전부 검색)
        IndexReader multiReader = new MultiReader(readers.toArray(new IndexReader[0]));
        IndexSearcher searcher = new IndexSearcher(multiReader);

        // 4) Analyzer 설정 (2-gram)
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                return new TokenStreamComponents(new NGramTokenizer(2, 2));
            }
        };

        // 5) Query 설정
        QueryParser parser = new QueryParser(isCaseSensitive ? "fileContent" : "fileContentLowercase", analyzer);
        parser.setDefaultOperator(QueryParser.Operator.AND); // 기본 연산을 AND로
        Query query = parser.parse(isCaseSensitive ? searchKeyword : searchKeyword.toLowerCase());

        // 6) 첫 검색, 페이징 검색 분기 처리
        TopDocs hits;
        if (lastScoreDocId <= 0) {
            hits = searcher.search(query, maxHits);
        } else {
            ScoreDoc lastScoreDoc = new ScoreDoc(lastScoreDocId, docScore);
            hits = searcher.searchAfter(lastScoreDoc, query, maxHits);
        }

        // 7) 파일 단위 검색
        List<SearchResultDto> results = new ArrayList<>();
        long totalCount = 0;

        for (ScoreDoc sd : hits.scoreDocs) {
            Document doc = searcher.doc(sd.doc);
            String fileContent = doc.get("fileContent");
            List<String> lineNumbers = findMatchedLines(fileContent, searchKeyword, isCaseSensitive);
            if (lineNumbers.isEmpty()) {
                continue;
            }
            results.add(new SearchResultDto(
                    doc.get("repositoryName"),
                    doc.get("fileName"),
                    doc.get("filePath"),
                    String.join(",", lineNumbers),
                    ""
            ));
        }

        if (hits.scoreDocs.length <= 0) {
            multiReader.close();
            throw new NoSearchResultException("검색 결과가 없습니다.");
        }

        // 현재 검색 결과의 마지막 도큐먼트 위치.
        ScoreDoc lastDoc = hits.scoreDocs[hits.scoreDocs.length - 1];

        // 8) 총 건수 가져오기
        TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(query, collector);
        totalCount = collector.getTotalHits();

        multiReader.close();
        return Pair.of(results, Pair.of(totalCount, new DocInfoDto(lastDoc.doc, lastDoc.score)));
    }

    private List<String> getRepositoryList() {
        File indexRoot = new File("index_line");
        if (!indexRoot.exists() || !indexRoot.isDirectory()) {
            return List.of();
        }

        File[] projectDirs = indexRoot.listFiles(File::isDirectory);
        if (projectDirs == null) {
            return List.of();
        }

        return Arrays.stream(projectDirs)
                .map(File::getName)
                .collect(Collectors.toList());
    }

    private List<String> findMatchedLines(String fileContent, String searchKeyword, boolean isCaseSensitive) {
        List<String> matchedLines = new ArrayList<>();
        String[] lines = fileContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (isCaseSensitive) {
                if (line.contains(searchKeyword)) {
                    // 성능은 떨어져도 2중 체크.
                    matchedLines.add(String.valueOf(i + 1));
                }
            } else {
                if (line.toLowerCase().contains(searchKeyword.toLowerCase())) {
                    matchedLines.add(String.valueOf(i + 1));
                }
            }
        }

        return matchedLines;
    }
}
