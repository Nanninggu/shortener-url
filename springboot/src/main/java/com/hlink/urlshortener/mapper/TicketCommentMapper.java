package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.TicketComment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TicketCommentMapper {
    void insert(TicketComment comment);
    List<TicketComment> findByTicketId(Long ticketId);
    TicketComment findById(Long id);
    void deleteById(Long id);
}

