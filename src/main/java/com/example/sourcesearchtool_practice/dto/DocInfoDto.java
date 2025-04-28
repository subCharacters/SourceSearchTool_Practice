package com.example.sourcesearchtool_practice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocInfoDto {
    private int lastScoreDocId;
    private float docScore;
}
