package com.example.sourcesearchtool_practice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultDto {
    private String repositoryName;
    private String fileName;
    private String filePath;
    private String lineNumber;
    private String lineContent;
}
