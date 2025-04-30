package com.example.sourcesearchtool_practice.indexing.job;

import com.example.sourcesearchtool_practice.indexing.tasklet.IndexingProcessorTasklet;
import com.example.sourcesearchtool_practice.util.IndexingCheck;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class IndexingJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final IndexingCheck indexingCheck;

    public IndexingJobConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager, IndexingCheck indexingCheck) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.indexingCheck = indexingCheck;
    }

    @Bean
    public Job indexingJob(Step indexingStep) {
        return new JobBuilder("indexingJob", jobRepository)
                .start(indexingStep)
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        indexingCheck.start(); // 인덱싱 시작 상태로 변경
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        indexingCheck.finish(); // 인덱싱 종료 상태로 변경
                    }
                })
                .build();
    }

    @Bean
    public Step indexingStep(IndexingProcessorTasklet IndexingProcessorTasklet) {
        return new StepBuilder("indexingStep", jobRepository)
                .tasklet(IndexingProcessorTasklet, transactionManager)
                .build();
    }
}
