package com.duo.controller;

import com.duo.common.R;
import com.duo.dto.CheckInDTO;
import com.duo.entity.CheckInRecord;
import com.duo.service.CheckInRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checkin")
public class CheckInController {

    @Autowired
    private CheckInRecordService checkInRecordService;

    @PostMapping("/submit")
    public R<String> checkIn(@RequestParam Long currentUserId, @RequestBody CheckInDTO checkInDTO) {
        try {
            checkInRecordService.checkIn(currentUserId, checkInDTO);
            return R.success("Check-in successful");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/status")
    public R<Map<String, Object>> getCheckInStatus(@RequestParam Long currentUserId, @RequestParam(required = false) Long partnerId) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Check own status for today specifically
            boolean myStatus = checkInRecordService.hasCheckedInOnDate(currentUserId, java.time.LocalDate.now().toString());
            result.put("mine", myStatus);
            
            // Fetch ALL records for calendar rendering
            List<CheckInRecord> myRecords = checkInRecordService.getAllCheckIns(currentUserId);
            result.put("myRecords", myRecords);
            
            // Check partner's status if they exist
            if (partnerId != null) {
                boolean partnerStatus = checkInRecordService.hasCheckedInOnDate(partnerId, java.time.LocalDate.now().toString());
                result.put("partner", partnerStatus);
                
                List<CheckInRecord> partnerRecords = checkInRecordService.getAllCheckIns(partnerId);
                result.put("partnerRecords", partnerRecords);
            }
            
            return R.success(result);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}
