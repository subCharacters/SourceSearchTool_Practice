package com.example.sourcesearchtool_practice.contorller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class IndexingController {
    private final JobLauncher jobLauncher;
    private final Job indexingJob;

    public IndexingController(JobLauncher jobLauncher, Job indexingJob) {
        this.jobLauncher = jobLauncher;
        this.indexingJob = indexingJob;
    }

    @PostMapping("/run-indexing")
    public ResponseEntity<String> runIndexing(@RequestParam String sourcePath) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("sourcePath", sourcePath)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(indexingJob, jobParameters);
        return ResponseEntity.ok("Indexing job started");
    }
}
