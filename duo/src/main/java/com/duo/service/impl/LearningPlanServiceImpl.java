package com.duo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duo.dto.LearningPlanDTO;
import com.duo.entity.LearningPlan;
import com.duo.mapper.LearningPlanMapper;
import com.duo.service.LearningPlanService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LearningPlanServiceImpl extends ServiceImpl<LearningPlanMapper, LearningPlan> implements LearningPlanService {

    @Override
    public void addPlan(Long userId, LearningPlanDTO planDTO) {
        LearningPlan plan = new LearningPlan();
        plan.setUserId(userId);
        plan.setContent(planDTO.getContent());
        plan.setDeadline(planDTO.getDeadline());
        plan.setStatus(0); // 0 = incomplete
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(plan);
    }

    @Override
    public void toggleStatus(Long pId, Long userId) {
        LearningPlan plan = baseMapper.selectById(pId);
        if (plan != null && plan.getUserId().equals(userId)) {
            plan.setStatus(plan.getStatus() == 0 ? 1 : 0);
            plan.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(plan);
        }
    }

    @Override
    public void deletePlan(Long pId, Long userId) {
        LearningPlan plan = baseMapper.selectById(pId);
        if (plan != null && plan.getUserId().equals(userId)) {
            baseMapper.deleteById(pId);
        }
    }

    @Override
    public List<LearningPlan> getPlansByUser(Long userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<LearningPlan>()
                .eq(LearningPlan::getUserId, userId)
                .orderByDesc(LearningPlan::getCreateTime)
        );
    }
}
