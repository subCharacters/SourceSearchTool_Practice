package com.example.sourcesearchtool_practice.service;

import com.example.sourcesearchtool_practice.dto.SearchResultDto;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneSimpleSearcherService {
    public List<SearchResultDto> search(String projectName, String keyword, int maxHits) throws Exception {
        List<SearchResultDto> results = new ArrayList<>();

        if (keyword.length() < 2) {
            return results; // 2자 미만이면 검색 안함
        }

        Path indexPath = Paths.get("index", projectName);
        Directory directory = FSDirectory.open(indexPath);
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                return new TokenStreamComponents(new NGramTokenizer(2, 2));
            }
        };

        QueryParser parser = new QueryParser("lineContent", analyzer);
        parser.setDefaultOperator(QueryParser.Operator.AND); // 기본 연산을 AND로
        Query query = parser.parse(keyword);

        TopDocs hits = searcher.search(query, maxHits);
        for (ScoreDoc sd : hits.scoreDocs) {
            Document doc = searcher.doc(sd.doc);
            results.add(new SearchResultDto(
                    doc.get("repositoryName"),
                    doc.get("fileName"),
                    doc.get("filePath"),
                    Integer.parseInt(doc.get("lineNumber")),
                    doc.get("lineContent")
            ));
        }
        // 총 건수 가져오기
        long totalCount = hits.totalHits.value;

        reader.close();
        return results;
        }
    }
