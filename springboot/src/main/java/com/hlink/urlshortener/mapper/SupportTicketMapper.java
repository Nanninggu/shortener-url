package com.hlink.urlshortener.mapper;

import com.hlink.urlshortener.model.SupportTicket;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SupportTicketMapper {
    void insert(SupportTicket ticket);
    Optional<SupportTicket> findById(Long id);
    List<SupportTicket> findByUserId(Long userId);
    List<SupportTicket> findByAssignedTo(Long assignedTo);
    List<SupportTicket> findByStatus(String status);
    List<SupportTicket> findAll();
    void update(SupportTicket ticket);
    void deleteById(Long id);
}

