package com.hlink.urlshortener.service;

import com.hlink.urlshortener.dto.SupportTicketCreateRequest;
import com.hlink.urlshortener.mapper.SupportTicketMapper;
import com.hlink.urlshortener.model.SupportTicket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketService {

    private final SupportTicketMapper supportTicketMapper;

    @Transactional
    public SupportTicket createTicket(SupportTicketCreateRequest request, Long userId) {
        String priority = request.getPriority() != null ? request.getPriority() : "NORMAL";
        
        SupportTicket ticket = SupportTicket.builder()
                .userId(userId)
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(priority)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        supportTicketMapper.insert(ticket);
        log.info("Support ticket created: {} by user {}", ticket.getSubject(), userId);
        return ticket;
    }

    public Optional<SupportTicket> findById(Long id) {
        return supportTicketMapper.findById(id);
    }

    public List<SupportTicket> findByUserId(Long userId) {
        return supportTicketMapper.findByUserId(userId);
    }

    public List<SupportTicket> findByAssignedTo(Long assignedTo) {
        return supportTicketMapper.findByAssignedTo(assignedTo);
    }

    public List<SupportTicket> findByStatus(String status) {
        return supportTicketMapper.findByStatus(status);
    }

    public List<SupportTicket> findAll() {
        return supportTicketMapper.findAll();
    }

    @Transactional
    public void updateTicket(SupportTicket ticket) {
        ticket.setUpdatedAt(LocalDateTime.now());
        supportTicketMapper.update(ticket);
    }

    @Transactional
    public void assignTicket(Long ticketId, Long assignedTo) {
        Optional<SupportTicket> ticketOpt = supportTicketMapper.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            ticket.setAssignedTo(assignedTo);
            ticket.setStatus("IN_PROGRESS");
            updateTicket(ticket);
        }
    }

    @Transactional
    public void resolveTicket(Long ticketId) {
        Optional<SupportTicket> ticketOpt = supportTicketMapper.findById(ticketId);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            ticket.setStatus("RESOLVED");
            updateTicket(ticket);
        }
    }

    @Transactional
    public void deleteTicket(Long id) {
        supportTicketMapper.deleteById(id);
    }
}

