package com.duo.controller;

import com.duo.common.R;
import com.duo.dto.BindDTO;
import com.duo.entity.CoupleRelation;
import com.duo.service.CoupleRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/couple")
public class CoupleRelationController {

    @Autowired
    private CoupleRelationService coupleRelationService;

    // TODO: In the future, the currentUserId should be parsed from the JWT Token in the request header
    // Currently we pass it as a path/query param for ease of testing MVP, or you can extract it inside if you have JWT filter

    @PostMapping("/bind")
    public R<String> bind(@RequestParam Long currentUserId, @RequestBody BindDTO bindDTO) {
        try {
            coupleRelationService.bindCouple(currentUserId, bindDTO);
            return R.success("Bound successfully");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @PostMapping("/unbind")
    public R<String> unbind(@RequestParam Long currentUserId) {
        try {
            coupleRelationService.unbindCouple(currentUserId);
            return R.success("Unbound successfully");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public R<com.duo.dto.RelationInfoDTO> getRelationInfo(@RequestParam Long currentUserId) {
        try {
            com.duo.dto.RelationInfoDTO infoDTO = coupleRelationService.getRelationInfoWithPartner(currentUserId);
            if (infoDTO == null) {
                return R.error(404, "Not bound yet");
            }
            return R.success(infoDTO);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}
