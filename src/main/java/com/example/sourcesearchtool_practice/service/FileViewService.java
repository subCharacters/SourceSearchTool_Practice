package com.example.sourcesearchtool_practice.service;

import com.example.sourcesearchtool_practice.dto.FileViewDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FileViewService {
    public FileViewDto getFileContentsFromLocalDir(String repositoryName, String fileName, String filePath, String lineNumber)
            throws IOException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            String[] lineNumbers = lineNumber.split(",");
            List<String> numberedLines = IntStream.range(1, lines.size())
                    .mapToObj((index) -> {
                        if (Arrays.stream(lineNumbers).anyMatch(String.valueOf(index)::equals)) {
                            return "<div style=\"background-color: yellow;\">" + index + " " + lines.get(index-1) + "</div>";
                        }
                        return "<div>" + index + " " + lines.get(index-1) + "</div>";
                    })
                    .collect(Collectors.toList());
            FileViewDto dto = new FileViewDto();
            dto.setContent(String.join("\n", numberedLines));
            return dto;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
