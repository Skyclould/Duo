package com.duo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duo.dto.BindDTO;
import com.duo.entity.CoupleRelation;

public interface CoupleRelationService extends IService<CoupleRelation> {
    void bindCouple(Long currentUserId, BindDTO bindDTO);

    void unbindCouple(Long currentUserId);

    CoupleRelation getRelationByUserId(Long userId);

    com.duo.dto.RelationInfoDTO getRelationInfoWithPartner(Long userId);
}
