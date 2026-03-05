package com.duo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duo.entity.SharedFile;
import com.duo.mapper.SharedFileMapper;
import com.duo.service.SharedFileService;
import org.springframework.stereotype.Service;

@Service
public class SharedFileServiceImpl extends ServiceImpl<SharedFileMapper, SharedFile> implements SharedFileService {
}
