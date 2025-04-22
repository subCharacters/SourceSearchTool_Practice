package com.example.sourcesearchtool_practice.indexing.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    // TODO: 실제 Lucene 인덱싱 로직 구현 필요
    private void indexProject(File projectDir) throws Exception {
        // 프로젝트 루트 기준으로 하위 파일 재귀 탐색,
        // 확장자 및 제외 폴더 필터 적용,
        // 각 라인별로 Lucene 인덱싱 수행
        try {
            // 해당 프로젝트 폴더 하위의 모든 파일을 재귀적으로 탐색
            Files.walk(projectDir.toPath())
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
                    });

        } catch (IOException e) {
            log.error("프로젝트 내 파일 탐색 실패: {}", projectDir.getAbsolutePath(), e);
        }
    }
}
