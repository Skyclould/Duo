package com.duo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duo.dto.CheckInDTO;
import com.duo.entity.CheckInRecord;

import java.util.List;

public interface CheckInRecordService extends IService<CheckInRecord> {
    
    void checkIn(Long currentUserId, CheckInDTO checkInDTO);
    
    boolean hasCheckedInOnDate(Long userId, String dateStr);
    
    List<CheckInRecord> getRecentCheckIns(Long userId, int days);
    
    List<CheckInRecord> getAllCheckIns(Long userId);
}
