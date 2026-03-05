package com.duo.controller;

import com.duo.common.R;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.error("File is empty");
        }
        
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + suffix;
        
        try {
            File dest = new File(uploadDir + newFilename);
            file.transferTo(dest.getAbsoluteFile());
            // Return URL that maps to this file
            String fileUrl = "/uploads/" + newFilename;
            return R.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return R.error("File upload failed: " + e.getMessage());
        }
    }
}
