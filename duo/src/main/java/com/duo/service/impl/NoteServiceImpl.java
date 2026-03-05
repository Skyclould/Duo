package com.duo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duo.entity.Note;
import com.duo.mapper.NoteMapper;
import com.duo.service.NoteService;
import org.springframework.stereotype.Service;

@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {
}
