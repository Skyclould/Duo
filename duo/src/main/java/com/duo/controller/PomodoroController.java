package com.duo.controller;

import com.duo.common.R;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/pomodoro")
public class PomodoroController {

    @Data
    public static class PomodoroState {
        private Long userId;
        private String status; // IDLE, FOCUSING
        private long startTime; // timestamp
        private int durationMinutes;
        private int todayFocusMinutes;
        private String lastUpdateDate;
        private long accumulatedFocusMs; // Tracks milliseconds passed before the last pause
    }

    private static final Map<Long, PomodoroState> stateMap = new ConcurrentHashMap<>();

    @GetMapping("/status")
    public R<Map<String, PomodoroState>> getStatus(@RequestParam Long currentUserId, @RequestParam(required = false) Long partnerId) {
        Map<String, PomodoroState> result = new HashMap<>();
        
        PomodoroState myState = getOrCreateState(currentUserId);
        result.put("mine", addLiveFocusMinutes(myState));
        
        if (partnerId != null) {
            PomodoroState partnerState = getOrCreateState(partnerId);
            result.put("partner", addLiveFocusMinutes(partnerState));
        }
        return R.success(result);
    }
    
    private PomodoroState addLiveFocusMinutes(PomodoroState original) {
        // Create a copy so we don't accidentally save the live accumulated minutes 
        // into the base state every single polling tick.
        PomodoroState displayState = new PomodoroState();
        displayState.setUserId(original.getUserId());
        displayState.setStatus(original.getStatus());
        displayState.setStartTime(original.getStartTime());
        displayState.setDurationMinutes(original.getDurationMinutes());
        displayState.setLastUpdateDate(original.getLastUpdateDate());
        displayState.setAccumulatedFocusMs(original.getAccumulatedFocusMs());
        
        long totalMs = original.getAccumulatedFocusMs();
        if ("FOCUSING".equals(original.getStatus())) {
            long now = System.currentTimeMillis();
            totalMs += (now - original.getStartTime());
        }
        
        int liveMinutes = (int) Math.round(totalMs / 60000.0);
        displayState.setTodayFocusMinutes(original.getTodayFocusMinutes() + Math.max(0, liveMinutes));
        return displayState;
    }

    @PostMapping("/start")
    public R<String> start(@RequestParam Long currentUserId, @RequestParam int durationMinutes) {
        PomodoroState state = getOrCreateState(currentUserId);
        state.setStatus("FOCUSING");
        state.setAccumulatedFocusMs(0);
        state.setStartTime(System.currentTimeMillis());
        state.setDurationMinutes(durationMinutes);
        return R.success("Pomodoro started");
    }

    @PostMapping("/finish")
    public R<String> finish(@RequestParam Long currentUserId) {
        PomodoroState state = getOrCreateState(currentUserId);
        if ("FOCUSING".equals(state.getStatus()) || "PAUSED".equals(state.getStatus())) {
            checkAndResetDate(state);
            long totalMs = state.getAccumulatedFocusMs();
            if ("FOCUSING".equals(state.getStatus())) {
                totalMs += (System.currentTimeMillis() - state.getStartTime());
            }
            int actualMinutes = (int) Math.round(totalMs / 60000.0);
            if (actualMinutes > 0) {
                state.setTodayFocusMinutes(state.getTodayFocusMinutes() + actualMinutes);
            }
            state.setStatus("IDLE");
            state.setStartTime(0);
            state.setAccumulatedFocusMs(0);
            state.setDurationMinutes(0);
        }
        return R.success("Pomodoro finished");
    }

    @PostMapping("/stop")
    public R<String> stop(@RequestParam Long currentUserId) {
        PomodoroState state = getOrCreateState(currentUserId);
        if ("FOCUSING".equals(state.getStatus()) || "PAUSED".equals(state.getStatus())) {
            checkAndResetDate(state);
            long totalMs = state.getAccumulatedFocusMs();
            if ("FOCUSING".equals(state.getStatus())) {
                totalMs += (System.currentTimeMillis() - state.getStartTime());
            }
            int actualMinutes = (int) Math.round(totalMs / 60000.0);
            if (actualMinutes > 0) {
                state.setTodayFocusMinutes(state.getTodayFocusMinutes() + actualMinutes);
            }
        }
        state.setStatus("IDLE");
        state.setStartTime(0);
        state.setAccumulatedFocusMs(0);
        state.setDurationMinutes(0);
        return R.success("Pomodoro stopped");
    }
    
    @PostMapping("/pause")
    public R<String> pause(@RequestParam Long currentUserId) {
        PomodoroState state = getOrCreateState(currentUserId);
        if ("FOCUSING".equals(state.getStatus())) {
            long now = System.currentTimeMillis();
            state.setAccumulatedFocusMs(state.getAccumulatedFocusMs() + (now - state.getStartTime()));
            state.setStatus("PAUSED");
            state.setStartTime(0); // Optional, signifies no longer actively running
        }
        return R.success("Pomodoro paused");
    }
    
    @PostMapping("/resume")
    public R<String> resume(@RequestParam Long currentUserId) {
        PomodoroState state = getOrCreateState(currentUserId);
        if ("PAUSED".equals(state.getStatus())) {
            state.setStatus("FOCUSING");
            state.setStartTime(System.currentTimeMillis());
        }
        return R.success("Pomodoro resumed");
    }

    private PomodoroState getOrCreateState(Long userId) {
        return stateMap.computeIfAbsent(userId, k -> {
            PomodoroState state = new PomodoroState();
            state.setUserId(userId);
            state.setStatus("IDLE");
            state.setTodayFocusMinutes(0);
            state.setAccumulatedFocusMs(0);
            state.setLastUpdateDate(java.time.LocalDate.now().toString());
            return state;
        });
    }

    private void checkAndResetDate(PomodoroState state) {
        String today = java.time.LocalDate.now().toString();
        if (!today.equals(state.getLastUpdateDate())) {
            state.setTodayFocusMinutes(0);
            state.setLastUpdateDate(today);
        }
    }
}
