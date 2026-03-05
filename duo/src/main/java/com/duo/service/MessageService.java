package com.duo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duo.dto.MessageDTO;
import com.duo.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {

    void sendMessage(Long currentUserId, MessageDTO messageDTO);

    List<Message> getMessageList(Long currentUserId, Boolean markAsRead);
}
