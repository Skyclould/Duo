package com.duo.dto;

import com.duo.entity.CoupleRelation;
import com.duo.entity.User;
import lombok.Data;

@Data
public class RelationInfoDTO {
    private CoupleRelation relation;
    private User partner;
}
