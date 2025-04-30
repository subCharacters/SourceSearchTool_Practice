package com.example.sourcesearchtool_practice.indexing.tasklet.legacy;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class IndexingTasklet implements Tasklet {
    @Value("${file.extensions}")
    private java.util.List<String> extensions;

    @Value("${file.excludedFolder}")
    private java.util.List<String> excludedFolders;

    // 스레드 개수
    private final int THREAD_COUNT = 3;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // TODO Listener
        log.info("Indexing tasklet");

        // JobParameters에서 sourcePath 꺼내기
        String sourcePath = (String) chunkContext.getStepContext().getJobParameters().get("sourcePath");

        // TODO Validation Check on Controller or Something else not here.
        if (sourcePath == null || sourcePath.isBlank()) {
            throw new IllegalArgumentException("sourcePath 파라미터가 필요합니다.");
        }

        // TODO Validation Check on Controller or Something else not here.
        File rootDir = new File(sourcePath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("유효한 폴더가 아님: " + sourcePath);
        }

        // 기존 인덱스 정리
        cleanUpOldIndexes(sourcePath);

        // 프로젝트 폴더 목록 추출
        File[] projectDirs = rootDir.listFiles(File::isDirectory);
        if (projectDirs == null || projectDirs.length == 0) {
            log.info("탐색할 프로젝트 폴더가 없음: {}", sourcePath);
            return RepeatStatus.FINISHED;
        }

        Queue<File> projectQueue = new ConcurrentLinkedQueue<>(Arrays.asList(projectDirs));
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                while (true) {
                    File project = projectQueue.poll();
                    if (project == null) break;

                    try {
                        log.info("프로젝트 인덱싱 시작: {}", project.getAbsolutePath());
                        // 실제 인덱싱 로직 호출 (아래 메서드는 직접 구현 필요)
                        indexProject(project);
                        log.info("프로젝트 인덱싱 완료: {}", project.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("프로젝트 인덱싱 실패: " + project.getAbsolutePath(), e);
                    }
                }
            });
        }

        executor.shutdown(); // 더 이상 새로운 작업은 받지 않음
        executor.awaitTermination(24, TimeUnit.HOURS); // 최대 24시간 동안 기다림

        log.info("모든 프로젝트 인덱싱 완료");
        return RepeatStatus.FINISHED;
    }

    private void cleanUpOldIndexes(String sourcePath) throws IOException {
        File sourceRoot = new File(sourcePath);
        File[] currentProjects = sourceRoot.listFiles(File::isDirectory);

        if (currentProjects == null) return;

        Set<String> currentProjectNames = Arrays.stream(currentProjects)
                .map(File::getName)
                .collect(Collectors.toSet());

        File[] indexRoots = {new File("index_line"), new File("index_file")};

        for (File indexRoot : indexRoots) {
            if (!indexRoot.exists()) {
                continue; // 인덱스 폴더가 아예 없으면 할 게 없음
            }

            File[] indexedProjects = indexRoot.listFiles(File::isDirectory);
            if (indexedProjects == null) continue;

            for (File indexed : indexedProjects) {
                if (!currentProjectNames.contains(indexed.getName())) {
                    // 인덱싱할 프로젝트가 아닌 경우 삭제
                    deleteDirectory(indexed);
                    log.info("삭제된 프로젝트 인덱스 정리: {}", indexed.getName());
                }
            }
        }
    }

    private void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!dir.delete()) {
            throw new IOException("Failed to delete: " + dir.getAbsolutePath());
        }
    }

    private void indexProject(File projectDir) throws Exception {
        // 프로젝트 루트 기준으로 하위 파일 재귀 탐색,
        // 확장자 및 제외 폴더 필터 적용,
        // 각 라인별로 Lucene 인덱싱 수행
        // 인덱스 저장 경로: index/프로젝트명
        Path lineIndexPath = Paths.get("index_line", projectDir.getName());
        Path fileIndexPath = Paths.get("index_file", projectDir.getName());
        Directory directory = FSDirectory.open(lineIndexPath);
        Directory fileDirectory = FSDirectory.open(fileIndexPath);

        // 2-gram 분석기 구성
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                // NGramTokenizer(minGram=2, maxGram=2): 2글자씩 자름
                return new TokenStreamComponents(new NGramTokenizer(2, 2));
            }
        };

        IndexWriterConfig lineConfig = new IndexWriterConfig(analyzer);
        lineConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // 인덱스 초기화

        IndexWriterConfig fileConfig = new IndexWriterConfig(analyzer);
        fileConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter lineWriter = new IndexWriter(directory, lineConfig);
        IndexWriter fileWriter = new IndexWriter(fileDirectory, fileConfig);

        try (Stream<Path> fileStream = Files.walk(projectDir.toPath())) {
            // 해당 프로젝트 폴더 하위의 모든 파일을 재귀적으로 탐색
            fileStream
                    .filter(Files::isRegularFile) // 디렉토리가 아닌 파일만 통과
                    .filter(path -> {
                        String filename = path.getFileName().toString().toLowerCase();
                        return extensions.stream().anyMatch(filename::endsWith); // 지정한 확장자로 끝나는 파일만 통과
                    })
                    .filter(path -> {
                        String fullPath = path.toString();
                        return excludedFolders.stream().noneMatch(fullPath::contains); // 제외 폴더가 경로에 포함된 파일은 제외
                    })
                    .forEach(path -> {
                        log.info("인덱싱 대상 파일: {}", path.toAbsolutePath());
                        try {
                            List<String> lines = Files.readAllLines(path);

                            // 행 단위 인덱싱
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i).trim();
                                if (line.isEmpty()) continue; // 공백 줄 제외
                                // 루신 문서 생성 및 필드 설정
                                Document doc = new Document();

                                doc.add(new StringField("repositoryName", projectDir.getName(), Field.Store.YES)); // 프로젝트명
                                doc.add(new StringField("fileName", path.getFileName().toString(), Field.Store.YES)); // 파일명
                                doc.add(new StringField("filePath", path.toString(), Field.Store.YES)); // 전체 경로
                                doc.add(new IntPoint("lineNumber", i + 1)); // 검색용 숫자 필드
                                doc.add(new StoredField("lineNumber", i + 1)); // 표시용 저장 필드
                                doc.add(new TextField("lineContent", line, Field.Store.YES)); // 2-gram 분석용 줄 내용
                                doc.add(new TextField("lineContentLowercase", line.toLowerCase(), Field.Store.YES)); // 대소문자 구문 안하는 용도

                                // 인덱스에 문서 추가
                                lineWriter.addDocument(doc);
                            }

                            // 파일 단위 인덱싱
                            String fullContent = String.join("\n", lines).trim();
                            if (!fullContent.isEmpty()) {
                                Document fileDoc = new Document();
                                fileDoc.add(new StringField("repositoryName", projectDir.getName(), Field.Store.YES));
                                fileDoc.add(new StringField("fileName", path.getFileName().toString(), Field.Store.YES));
                                fileDoc.add(new StringField("filePath", path.toString(), Field.Store.YES));
                                fileDoc.add(new TextField("fileContent", fullContent, Field.Store.YES));
                                fileDoc.add(new TextField("fileContentLowercase", fullContent.toLowerCase(), Field.Store.YES)); // 대소문자 구문 안하는 용도

                                // 인덱스에 문서 추가
                                fileWriter.addDocument(fileDoc);
                            }
                        } catch (IOException e) {
                            log.error("인덱싱 실패: {}", path, e);
                        }
                    });

        } catch (IOException e) {
            log.error("프로젝트 내 파일 탐색 실패: {}", projectDir.getAbsolutePath(), e);
        }

        // 인덱스 커밋 및 종료
        lineWriter.commit();
        lineWriter.close();

        fileWriter.commit();
        fileWriter.close();

        log.info("인덱스 저장 완료: {}", lineIndexPath);
    }
}
