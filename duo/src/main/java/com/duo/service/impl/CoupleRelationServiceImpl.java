package com.duo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duo.dto.BindDTO;
import com.duo.entity.CoupleRelation;
import com.duo.entity.User;
import com.duo.mapper.CoupleRelationMapper;
import com.duo.mapper.UserMapper;
import com.duo.service.CoupleRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CoupleRelationServiceImpl extends ServiceImpl<CoupleRelationMapper, CoupleRelation> implements CoupleRelationService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindCouple(Long currentUserId, BindDTO bindDTO) {
        // Find partner by invite code
        User partner = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getInviteCode, bindDTO.getInviteCode()));

        if (partner == null) {
            throw new RuntimeException("Invalid invite code");
        }

        if (partner.getId().equals(currentUserId)) {
            throw new RuntimeException("Cannot bind with yourself");
        }

        // Check if either is already bound (status = 1)
        checkAlreadyBound(currentUserId);
        checkAlreadyBound(partner.getId());

        // Create new relation
        CoupleRelation relation = new CoupleRelation();
        // Keep order for consistency or not, A and B are symmetric here.
        relation.setUserAId(Math.min(currentUserId, partner.getId()));
        relation.setUserBId(Math.max(currentUserId, partner.getId()));
        relation.setStatus(1);
        relation.setSharedGoal(bindDTO.getSharedGoal());
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(relation);
    }

    @Override
    public void unbindCouple(Long currentUserId) {
        CoupleRelation relation = getRelationByUserId(currentUserId);
        if (relation == null) {
            throw new RuntimeException("No active binding found");
        }

        relation.setStatus(0);
        relation.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(relation);
    }

    @Override
    public CoupleRelation getRelationByUserId(Long userId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<CoupleRelation>()
                .eq(CoupleRelation::getStatus, 1) // active binding
                .and(wrapper -> wrapper.eq(CoupleRelation::getUserAId, userId)
                        .or()
                        .eq(CoupleRelation::getUserBId, userId))
        );
    }

    @Override
    public com.duo.dto.RelationInfoDTO getRelationInfoWithPartner(Long userId) {
        CoupleRelation relation = getRelationByUserId(userId);
        if (relation == null) {
            return null;
        }
        Long partnerId = relation.getUserAId().equals(userId) ? relation.getUserBId() : relation.getUserAId();
        User partner = userMapper.selectById(partnerId);
        if (partner != null) {
            partner.setPassword(null);
        }
        
        com.duo.dto.RelationInfoDTO dto = new com.duo.dto.RelationInfoDTO();
        dto.setRelation(relation);
        dto.setPartner(partner);
        return dto;
    }

    private void checkAlreadyBound(Long userId) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<CoupleRelation>()
                .eq(CoupleRelation::getStatus, 1)
                .and(wrapper -> wrapper.eq(CoupleRelation::getUserAId, userId)
                        .or()
                        .eq(CoupleRelation::getUserBId, userId))
        );
        if (count > 0) {
            throw new RuntimeException("User " + userId + " is already bound");
        }
    }
}
