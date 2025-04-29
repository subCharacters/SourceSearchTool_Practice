package com.example.sourcesearchtool_practice.service;

import com.example.sourcesearchtool_practice.dto.DocInfoDto;
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
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LuceneSimpleSearcherService {
    public Pair<List<SearchResultDto>, Pair<Long, DocInfoDto>> search(
            List<String> projectNames,
            String keyword,
            String caseSensitive,
            String searchType,
            String fileExtension,
            int lastScoreDocId,
            float score,
            int maxHits) throws Exception {

        log.info("\n====== Search Parameters ======\n" +
                        "🔹 Project(s): {}\n" +
                        "🔹 Keyword: '{}'\n" +
                        "🔹 Case Sensitive: {}\n" +
                        "🔹 Search Type: {}\n" +
                        "🔹 File Extension: {}\n" +
                        "🔹 Max Hits: {}\n" +
                        "🔹 lastScoreDocId: {}\n" +
                        "🔹 score: {}\n" +
                        "===============================",
                projectNames != null ? projectNames : "전체",
                keyword,
                caseSensitive,
                searchType,
                fileExtension,
                maxHits,
                lastScoreDocId,
                score
        );

        List<SearchResultDto> results = new ArrayList<>();

        if (keyword.length() < 2) {
            return Pair.of(results, Pair.of(0L, new DocInfoDto(0, 0f))); // 2자 미만이면 검색 안함
        }

        // 프로젝트가 비어있으면 전체 가져오기
        if (projectNames == null || projectNames.isEmpty()) {
            projectNames = getRepositoryList();
        }

        // 선택한 프로젝트들의 인덱스 Reader를 연다
        List<IndexReader> readers = new ArrayList<>();

        String targetFolder = searchType.equals("file") ? "index_file" : "index_line";
        for (String projectName : projectNames) {
            Path indexPath = Paths.get(targetFolder, projectName);

            Directory directory = FSDirectory.open(indexPath);
            readers.add(DirectoryReader.open(directory));
        }

        // 모든 Reader를 MultiReader로 묶는다
        IndexReader multiReader = new MultiReader(readers.toArray(new IndexReader[0]));
        IndexSearcher searcher = new IndexSearcher(multiReader);

        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                return new TokenStreamComponents(new NGramTokenizer(2, 2));
            }
        };
        QueryParser parser = null;
        Query query = null;
        if (searchType.equals("file")) {
            parser = new QueryParser(caseSensitive.equals("ignore") ? "fileContentLowercase" : "fileContent", analyzer);
        } else {
            parser = new QueryParser(caseSensitive.equals("ignore") ? "lineContentLowercase" : "lineContent", analyzer);
        }
        parser.setDefaultOperator(QueryParser.Operator.AND); // 기본 연산을 AND로
        query = parser.parse(caseSensitive.equals("ignore") ? keyword.toLowerCase() : keyword);

        TopDocs hits;
        if (lastScoreDocId <= 0) {
            hits = searcher.search(query, maxHits);
        } else {
            ScoreDoc lastScoreDoc = new ScoreDoc(lastScoreDocId, score);
            hits = searcher.searchAfter(lastScoreDoc, query, maxHits);
        }

        long totalCount = 0;
        for (ScoreDoc sd : hits.scoreDocs) {
            Document doc = searcher.doc(sd.doc);

            if (searchType.equals("file")) { // 파일단위 검색
                String fileContent = doc.get("fileContent");
                List<String> lineNumbers = findMatchedLines(fileContent, keyword, caseSensitive);
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
            } else { // 행 단위 검색
                results.add(new SearchResultDto(
                        doc.get("repositoryName"),
                        doc.get("fileName"),
                        doc.get("filePath"),
                        doc.get("lineNumber"),
                        doc.get(caseSensitive.equals("ignore") ? "lineContentLowercase" : "lineContent")
                ));
            }

        }

        // 총 건수 가져오기
        // TODO 페이징 검색일때는 안하는게 좋음.
        TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(query, collector);
        totalCount = collector.getTotalHits();

        log.info(String.valueOf(totalCount));

        if (hits.scoreDocs.length <= 0) {
            multiReader.close();
            throw new NoSearchResultException("검색 결과가 없습니다.");
        }

        ScoreDoc lastDoc = hits.scoreDocs[hits.scoreDocs.length - 1];

        multiReader.close();
        return Pair.of(results, Pair.of(totalCount, new

                DocInfoDto(lastDoc.doc, lastDoc.score)));
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

    private List<String> findMatchedLines(String fileContent, String keyword, String caseSensitive) {
        List<String> matchedLines = new ArrayList<>();
        String[] lines = fileContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if ("ignore".equals(caseSensitive)) {
                if (line.toLowerCase().contains(keyword.toLowerCase())) {
                    matchedLines.add(String.valueOf(i + 1));
                }
            } else {
                if (line.contains(keyword)) {
                    // 성능은 떨어져도 2중 체크.
                    matchedLines.add(String.valueOf(i + 1));
                }
            }
        }

        return matchedLines;
    }
}
