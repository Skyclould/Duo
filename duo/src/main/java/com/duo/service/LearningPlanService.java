package com.duo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duo.dto.LearningPlanDTO;
import com.duo.entity.LearningPlan;

import java.util.List;

public interface LearningPlanService extends IService<LearningPlan> {

    void addPlan(Long userId, LearningPlanDTO planDTO);

    void toggleStatus(Long pId, Long userId);

    void deletePlan(Long pId, Long userId);

    List<LearningPlan> getPlansByUser(Long userId);
}
