package com.duo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duo.common.R;
import com.duo.entity.Note;
import com.duo.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping("/get")
    public R<Note> getNote(@RequestParam Long userId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        Note note = noteService.getOne(wrapper);
        if (note == null) {
            note = new Note();
            note.setUserId(userId);
            note.setTitle("My Markdown Notes");
            note.setContent("# My Notes\n\nStart writing here...");
            note.setCreateTime(LocalDateTime.now());
            note.setUpdateTime(LocalDateTime.now());
            noteService.save(note);
        }
        return R.success(note);
    }

    @PostMapping("/save")
    public R<String> saveNote(@RequestBody Note note) {
        note.setUpdateTime(LocalDateTime.now());
        if (note.getId() != null) {
            noteService.updateById(note);
        } else {
            note.setCreateTime(LocalDateTime.now());
            noteService.save(note);
        }
        return R.success("Note saved successfully");
    }
}
