package com.duo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duo.dto.MessageDTO;
import com.duo.entity.CoupleRelation;
import com.duo.entity.Message;
import com.duo.mapper.MessageMapper;
import com.duo.service.CoupleRelationService;
import com.duo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private CoupleRelationService coupleRelationService;

    @Override
    public void sendMessage(Long currentUserId, MessageDTO messageDTO) {
        // Retrieve relation to find the partner ID
        CoupleRelation relation = coupleRelationService.getRelationByUserId(currentUserId);
        if (relation == null) {
            throw new RuntimeException("You have not bound a partner yet");
        }

        Long partnerId = relation.getUserAId().equals(currentUserId) ? relation.getUserBId() : relation.getUserAId();

        Message message = new Message();
        message.setSenderId(currentUserId);
        message.setReceiverId(partnerId);
        message.setContent(messageDTO.getContent());
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        baseMapper.insert(message);
    }

    @Override
    public List<Message> getMessageList(Long currentUserId, Boolean markAsRead) {
        // Retrieve relation to find the partner ID
        CoupleRelation relation = coupleRelationService.getRelationByUserId(currentUserId);
        if (relation == null) {
            throw new RuntimeException("You have not bound a partner yet");
        }
        
        Long partnerId = relation.getUserAId().equals(currentUserId) ? relation.getUserBId() : relation.getUserAId();

        // Query all messages between the two users
        List<Message> messages = baseMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getSenderId, currentUserId).eq(Message::getReceiverId, partnerId)
                        .or()
                        .eq(Message::getSenderId, partnerId).eq(Message::getReceiverId, currentUserId))
                .orderByAsc(Message::getCreateTime));
        
        if (Boolean.TRUE.equals(markAsRead)) {
            // Mark received messages as read
            messages.forEach(msg -> {
                if (msg.getReceiverId().equals(currentUserId) && msg.getIsRead() == 0) {
                    msg.setIsRead(1);
                    baseMapper.updateById(msg);
                }
            });
        }
        
        return messages;
    }
}
