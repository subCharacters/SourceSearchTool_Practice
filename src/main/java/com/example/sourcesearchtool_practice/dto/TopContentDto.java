package com.example.sourcesearchtool_practice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopContentDto {
    private List<String> repositoryName;
    private List<String> extensions;
}
