package com.secondhand.controller;

import com.secondhand.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 本地图片上传（不需要阿里云 OSS 也能发布带图商品）。
 * 文件保存在 app.upload-dir 下，通过 /uploads/** 访问。
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Set<String> ALLOWED_EXT =
            new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp"));
    private static final long MAX_SIZE = 5 * 1024 * 1024L;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("图片不能超过 5MB");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new RuntimeException("只支持 jpg / jpeg / png / gif / webp 图片");
        }

        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(month);
        Files.createDirectories(dir);

        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        file.transferTo(dir.resolve(fileName).toFile());

        return Result.success("/uploads/" + month + "/" + fileName);
    }
}
