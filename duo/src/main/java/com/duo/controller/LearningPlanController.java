package com.duo.controller;

import com.duo.common.R;
import com.duo.dto.LearningPlanDTO;
import com.duo.entity.LearningPlan;
import com.duo.service.LearningPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plan")
public class LearningPlanController {

    @Autowired
    private LearningPlanService learningPlanService;

    @PostMapping("/add")
    public R<String> addPlan(@RequestParam Long currentUserId, @RequestBody LearningPlanDTO planDTO) {
        try {
            learningPlanService.addPlan(currentUserId, planDTO);
            return R.success("Plan added successfully");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @PostMapping("/toggle/{id}")
    public R<String> toggleStatus(@PathVariable Long id, @RequestParam Long currentUserId) {
        try {
            learningPlanService.toggleStatus(id, currentUserId);
            return R.success("Plan toggled successfully");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public R<String> deletePlan(@PathVariable Long id, @RequestParam Long currentUserId) {
        try {
            learningPlanService.deletePlan(id, currentUserId);
            return R.success("Plan deleted successfully");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public R<Map<String, List<LearningPlan>>> getPlanList(@RequestParam Long currentUserId, @RequestParam(required = false) Long partnerId) {
        try {
            Map<String, List<LearningPlan>> result = new HashMap<>();
            
            List<LearningPlan> myPlans = learningPlanService.getPlansByUser(currentUserId);
            result.put("mine", myPlans);
            
            if (partnerId != null) {
                List<LearningPlan> partnerPlans = learningPlanService.getPlansByUser(partnerId);
                result.put("partner", partnerPlans);
            }
            
            return R.success(result);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}
