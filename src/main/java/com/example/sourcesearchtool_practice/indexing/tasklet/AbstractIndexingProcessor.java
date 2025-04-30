package com.example.sourcesearchtool_practice.indexing.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractIndexingProcessor {
    protected abstract Directory createDirectory(File projectDir) throws IOException;
    protected abstract IndexWriter getIndexWriter(Directory directory) throws IOException;
    protected abstract void index(Path filePath, Directory directory, File projectDir, IndexWriter writer) throws Exception;
    protected abstract List<String> getExtensions();
    protected abstract List<String> getExcludedFolders();

    public void process(File projectDir) throws Exception {
        log.info("프로젝트 인덱싱 시작: {}", projectDir.getAbsolutePath());
        Directory directory = createDirectory(projectDir);
        IndexWriter writer = getIndexWriter(directory);
        try (Stream<Path> fileStream = Files.walk(projectDir.toPath())) {
            fileStream
                    .filter(Files::isRegularFile)
                    .filter(this::isExtensionMatched)
                    .filter(this::isNotInExcludedFolder)
                    .forEach(path -> {
                        try {
                            index(path, directory, projectDir, writer);
                        } catch (Exception e) {
                            log.error("파일 인덱싱 실패: {}", path, e);
                        }
                    });
        }

        writer.commit();
        writer.close();
        closeDirectory(directory);
        log.info("프로젝트 인덱싱 완료: {}", projectDir.getAbsolutePath());
    }

    protected boolean isExtensionMatched(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return getExtensions().stream().anyMatch(filename::endsWith);
    }

    protected boolean isNotInExcludedFolder(Path path) {
        String fullPath = path.toString();
        return getExcludedFolders().stream().noneMatch(fullPath::contains);
    }

    protected void closeDirectory(Directory directory) throws IOException {
        if (directory != null) {
            directory.close();
        }
    }
}
