package com.example.sourcesearchtool_practice.indexing.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class IndexingJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public IndexingJobConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job indexingJob(Step indexingStep) {
        return new JobBuilder("indexingJob", jobRepository)
                .start(indexingStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step indexingStep(Tasklet IndexingTasklet) {
        return new StepBuilder("indexingStep", jobRepository)
                .tasklet(IndexingTasklet, transactionManager)
                .build();
    }
}
