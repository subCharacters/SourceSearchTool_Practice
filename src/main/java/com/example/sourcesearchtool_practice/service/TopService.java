package com.example.sourcesearchtool_practice.service;

import com.example.sourcesearchtool_practice.dto.TopContentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopService {
    @Value("${file.extensions}")
    private List<String> extensions;

    public TopContentDto getTopRepoContents() {
        File indexRoot = new File("index_line");
        TopContentDto topContentDto = new TopContentDto();
        topContentDto.setExtensions(extensions);
        if (!indexRoot.exists() || !indexRoot.isDirectory()) {
            topContentDto.setRepositoryName(List.of());
            return topContentDto; // 인덱스 폴더가 없으면 빈 리스트
        }

        File[] projectDirs = indexRoot.listFiles(File::isDirectory);
        if (projectDirs == null) {
            topContentDto.setRepositoryName(List.of());
            return topContentDto;
        }

        topContentDto.setRepositoryName(Arrays.stream(projectDirs)
                .map(File::getName)
                .collect(Collectors.toList()));
        return topContentDto;
    }
}
