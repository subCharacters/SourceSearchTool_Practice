package com.example.sourcesearchtool_practice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResultDto {
    private String repositoryName;
    private String fileName;
    private String filePath;
    private int lineNumber;
    private String lineContent;
}
