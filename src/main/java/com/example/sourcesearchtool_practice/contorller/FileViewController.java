package com.example.sourcesearchtool_practice.contorller;

import com.example.sourcesearchtool_practice.dto.FileViewDto;
import com.example.sourcesearchtool_practice.service.FileViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/file")
public class FileViewController {

    private final FileViewService fileViewService;

    public FileViewController(FileViewService fileViewService) {
        this.fileViewService = fileViewService;
    }

    @GetMapping("/viewer")
    public String view(
            @RequestParam String repositoryName,
            @RequestParam String fileName,
            @RequestParam String filePath,
            @RequestParam String lineNumber,
            Model model) throws IOException {
        log.info("RequestParams: {}", repositoryName, fileName, filePath, lineNumber);

        FileViewDto dto = new FileViewDto();
        dto = fileViewService.getFileContentsFromLocalDir(repositoryName, fileName, filePath, lineNumber);

        if (dto == null) {
            return "error/404";
        }

        model.addAttribute("content", dto.getContent());
        return "viewer";
    }
}
