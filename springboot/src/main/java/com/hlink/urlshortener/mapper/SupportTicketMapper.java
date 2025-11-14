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
    List<SupportTicket> findByPriority(String priority);
    List<SupportTicket> findAll();
    List<SupportTicket> findAllWithPaging(@org.apache.ibatis.annotations.Param("offset") int offset, @org.apache.ibatis.annotations.Param("size") int size);
    List<SupportTicket> findByUserIdWithPaging(@org.apache.ibatis.annotations.Param("userId") Long userId, @org.apache.ibatis.annotations.Param("offset") int offset, @org.apache.ibatis.annotations.Param("size") int size);
    List<SupportTicket> findByPriorityWithPaging(@org.apache.ibatis.annotations.Param("priority") String priority, @org.apache.ibatis.annotations.Param("offset") int offset, @org.apache.ibatis.annotations.Param("size") int size);
    long countAll();
    long countByUserId(@org.apache.ibatis.annotations.Param("userId") Long userId);
    long countByPriority(@org.apache.ibatis.annotations.Param("priority") String priority);
    void update(SupportTicket ticket);
    void deleteById(Long id);
}

