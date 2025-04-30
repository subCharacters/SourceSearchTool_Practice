package com.example.sourcesearchtool_practice.indexing.tasklet;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
@Component
public class FileIndexingProcessor extends AbstractIndexingProcessor {
    @Value("${file.extensions}")
    private List<String> extensions;

    @Value("${file.excludedFolder}")
    private List<String> excludedFolders;

    @Override
    protected Directory createDirectory(File projectDir) throws IOException {
        // 인덱스 저장 경로
        Path indexPath = Paths.get("index_file", projectDir.getName());
        Directory directory = FSDirectory.open(indexPath);

        return directory;
    }

    @Override
    protected IndexWriter getIndexWriter(Directory directory) throws IOException {
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                // NGramTokenizer(minGram=2, maxGram=2): 2글자씩 자름
                return new TokenStreamComponents(new NGramTokenizer(2, 2));
            }
        };

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        return new IndexWriter(directory, config);
    }

    @Override
    protected void index(Path filePath, Directory directory, File projectDir, IndexWriter writer) throws Exception {

        List<String> lines = Files.readAllLines(filePath);
        String fullContent = String.join("\n", lines).trim();
        if (!fullContent.isEmpty()) {
            Document fileDoc = new Document();
            fileDoc.add(new StringField("repositoryName", projectDir.getName(), Field.Store.YES));
            fileDoc.add(new StringField("fileName", filePath.getFileName().toString(), Field.Store.YES));
            fileDoc.add(new StringField("filePath", filePath.toString(), Field.Store.YES));
            fileDoc.add(new TextField("fileContent", fullContent, Field.Store.YES));
            fileDoc.add(new TextField("fileContentLowercase", fullContent.toLowerCase(), Field.Store.YES)); // 대소문자 구문 안하는 용도

            // 인덱스에 문서 추가
            writer.addDocument(fileDoc);
        }
    }

    @Override
    protected void closeDirectory(Directory directory) throws IOException {
        super.closeDirectory(directory);
    }

    @Override
    protected List<String> getExtensions() {
        return extensions;
    }

    @Override
    protected List<String> getExcludedFolders() {
        return excludedFolders;
    }
}
