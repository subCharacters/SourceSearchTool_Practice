package com.example.sourcesearchtool_practice.indexing.tasklet;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.document.*;
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
public class LineIndexingProcessor extends AbstractIndexingProcessor {
    @Value("${file.extensions}")
    private List<String> extensions;

    @Value("${file.excludedFolder}")
    private List<String> excludedFolders;

    private IndexWriter writer;

    @Override
    protected Directory createDirectory(File projectDir) throws IOException {
        // 인덱스 저장 경로
        Path indexPath = Paths.get("index_line", projectDir.getName());
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
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            Document doc = new Document();
            doc.add(new StringField("repositoryName", projectDir.getName(), Field.Store.YES));
            doc.add(new StringField("fileName", filePath.getFileName().toString(), Field.Store.YES));
            doc.add(new StringField("filePath", filePath.toString(), Field.Store.YES));
            doc.add(new IntPoint("lineNumber", i + 1));
            doc.add(new StoredField("lineNumber", i + 1));
            doc.add(new TextField("lineContent", line, Field.Store.YES));
            doc.add(new TextField("lineContentLowercase", line.toLowerCase(), Field.Store.YES));

            writer.addDocument(doc);
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
