package com.example.sourcesearchtool_practice.contorller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class IndexingController {
    private final JobLauncher jobLauncher;
    private final Job indexingJob;

    public IndexingController(JobLauncher jobLauncher, Job indexingJob) {
        this.jobLauncher = jobLauncher;
        this.indexingJob = indexingJob;
    }

    @GetMapping("/logs/application.log")
    public ResponseEntity<Resource> downloadLog() throws IOException {
        Path logPath = Paths.get("logs", "application.log");
        Resource resource = new FileSystemResource(logPath.toFile());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=application.log")
                .contentType(MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE + "; charset=UTF-8"))
                .body(resource);
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
