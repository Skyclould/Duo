package com.duo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duo.common.R;
import com.duo.entity.SharedFile;
import com.duo.service.SharedFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/sharedFile")
public class SharedFileController {

    @Autowired
    private SharedFileService sharedFileService;

    @PostMapping("/add")
    public R<String> add(@RequestBody SharedFile sharedFile) {
        sharedFile.setCreateTime(LocalDateTime.now());
        sharedFileService.save(sharedFile);
        return R.success("Upload successful");
    }

    @GetMapping("/list")
    public R<List<SharedFile>> list(@RequestParam Long currentUserId, @RequestParam(required = false) Long partnerId) {
        if (partnerId == null) {
            return R.success(List.of());
        }
        LambdaQueryWrapper<SharedFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq(SharedFile::getUploaderId, currentUserId).eq(SharedFile::getPartnerId, partnerId)
                .or()
                .eq(SharedFile::getUploaderId, partnerId).eq(SharedFile::getPartnerId, currentUserId)
        );
        queryWrapper.orderByDesc(SharedFile::getCreateTime);
        return R.success(sharedFileService.list(queryWrapper));
    }

    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Long id, @RequestParam Long currentUserId) {
        // Simple permission check: must be uploader
        SharedFile file = sharedFileService.getById(id);
        if (file == null) {
            return R.error("File not found");
        }
        if (!file.getUploaderId().equals(currentUserId)) {
            return R.error("Can only delete your own files");
        }
        sharedFileService.removeById(id);
        return R.success("Delete successful");
    }
}
