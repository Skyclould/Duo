package com.duo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duo.dto.CheckInDTO;
import com.duo.entity.CheckInRecord;
import com.duo.mapper.CheckInRecordMapper;
import com.duo.service.CheckInRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckInRecordServiceImpl extends ServiceImpl<CheckInRecordMapper, CheckInRecord> implements CheckInRecordService {

    @Override
    public void checkIn(Long currentUserId, CheckInDTO checkInDTO) {
        String dateStr = checkInDTO.getCheckInDate() != null ? checkInDTO.getCheckInDate() : LocalDate.now().toString();

        CheckInRecord record = new CheckInRecord();
        record.setUserId(currentUserId);
        record.setDurationMinutes(checkInDTO.getDurationMinutes());
        record.setTimeRange(checkInDTO.getTimeRange());
        record.setContent(checkInDTO.getContent());
        record.setImageUrl(checkInDTO.getImageUrl());
        record.setFileUrl(checkInDTO.getFileUrl());
        record.setCheckInDate(LocalDate.parse(dateStr));
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(record);
    }

    @Override
    public boolean hasCheckedInOnDate(Long userId, String dateStr) {
        LocalDate date = dateStr == null ? LocalDate.now() : LocalDate.parse(dateStr);
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getUserId, userId)
                .eq(CheckInRecord::getCheckInDate, date)
        );
        return count > 0;
    }

    @Override
    public List<CheckInRecord> getRecentCheckIns(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return baseMapper.selectList(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getUserId, userId)
                .ge(CheckInRecord::getCheckInDate, startDate)
                .orderByDesc(CheckInRecord::getCheckInDate)
        );
    }
    
    @Override
    public List<CheckInRecord> getAllCheckIns(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<CheckInRecord>()
                .eq(CheckInRecord::getUserId, userId)
                .orderByDesc(CheckInRecord::getCheckInDate)
        );
    }
}
