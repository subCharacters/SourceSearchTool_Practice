package com.example.sourcesearchtool_practice.indexing.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class IndexingProcessorTasklet implements Tasklet {
    private final LineIndexingProcessor lineIndexingProcessor;
    private final FileIndexingProcessor fileIndexingProcessor;

    private final int THREAD_COUNT = 3;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("IndexingProcessTasklet 시작");

        String sourcePath = (String) chunkContext.getStepContext().getJobParameters().get("sourcePath");

        if (sourcePath == null || sourcePath.isBlank()) {
            throw new IllegalArgumentException("sourcePath 파라미터가 필요합니다.");
        }

        File rootDir = new File(sourcePath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("유효한 폴더가 아님: " + sourcePath);
        }

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
                        log.info("라인 인덱싱 시작: {}", project.getAbsolutePath());
                        lineIndexingProcessor.process(project);

                        log.info("파일 인덱싱 시작: {}", project.getAbsolutePath());
                        fileIndexingProcessor.process(project);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        log.info("모든 프로젝트 인덱싱 완료");
        return RepeatStatus.FINISHED;
    }
}
