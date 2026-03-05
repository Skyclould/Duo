package com.duo.controller;

import com.duo.common.R;
import com.duo.dto.MessageDTO;
import com.duo.entity.Message;
import com.duo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public R<String> sendMessage(@RequestParam Long currentUserId, @RequestBody MessageDTO messageDTO) {
        try {
            messageService.sendMessage(currentUserId, messageDTO);
            return R.success("Send message successfully");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public R<List<Message>> getMessageList(@RequestParam Long currentUserId,
                                           @RequestParam(required = false, defaultValue = "false") Boolean markAsRead) {
        try {
            List<Message> list = messageService.getMessageList(currentUserId, markAsRead);
            return R.success(list);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}
