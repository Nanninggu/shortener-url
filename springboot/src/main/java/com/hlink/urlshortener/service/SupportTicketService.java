package com.hlink.urlshortener.service;

import com.hlink.urlshortener.dto.SupportTicketCreateRequest;
import com.hlink.urlshortener.mapper.SupportTicketMapper;
import com.hlink.urlshortener.mapper.TicketCommentMapper;
import com.hlink.urlshortener.mapper.UserMapper;
import com.hlink.urlshortener.model.SupportTicket;
import com.hlink.urlshortener.model.TicketComment;
import com.hlink.urlshortener.model.User;
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
    private final TicketCommentMapper ticketCommentMapper;
    private final UserMapper userMapper;

    @Transactional
    public SupportTicket createTicket(SupportTicketCreateRequest request, Long userId) {
        try {
            String priority = request.getPriority() != null && !request.getPriority().isEmpty() 
                    ? request.getPriority() : "NORMAL";
            
            if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수입니다");
            }
            
            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("내용은 필수입니다");
            }
            
            // 사용자 존재 여부 확인
            Optional<User> userOpt = userMapper.findById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("사용자 ID " + userId + "가 존재하지 않습니다. 먼저 사용자를 생성해주세요.");
            }
            
            SupportTicket ticket = SupportTicket.builder()
                    .userId(userId)
                    .subject(request.getSubject().trim())
                    .description(request.getDescription().trim())
                    .priority(priority)
                    .status("OPEN")
                    .assignedTo(null) // 초기에는 할당되지 않음
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            supportTicketMapper.insert(ticket);
            log.info("Support ticket created: {} by user {}", ticket.getSubject(), userId);
            return ticket;
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating support ticket: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating support ticket: {}", e.getMessage(), e);
            throw new RuntimeException("티켓 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
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

    /**
     * 우선순위 기반 티켓 목록 조회 (HIGH > MEDIUM > NORMAL > LOW 순서)
     */
    public List<SupportTicket> findAllByPriority() {
        List<SupportTicket> allTickets = supportTicketMapper.findAll();
        // 우선순위 순서: HIGH > MEDIUM > NORMAL > LOW
        allTickets.sort((t1, t2) -> {
            int priority1 = getPriorityValue(t1.getPriority());
            int priority2 = getPriorityValue(t2.getPriority());
            if (priority1 != priority2) {
                return Integer.compare(priority2, priority1); // 높은 우선순위가 먼저
            }
            // 우선순위가 같으면 생성일 기준 내림차순
            return t2.getCreatedAt().compareTo(t1.getCreatedAt());
        });
        return allTickets;
    }

    /**
     * 우선순위별 티켓 조회
     */
    public List<SupportTicket> findByPriority(String priority) {
        return supportTicketMapper.findByPriority(priority);
    }

    /**
     * 모든 티켓 페이지네이션 조회
     */
    public com.hlink.urlshortener.dto.PageResponse<SupportTicket> findAllWithPaging(int page, int size) {
        int offset = page * size;
        List<SupportTicket> tickets = supportTicketMapper.findAllWithPaging(offset, size);
        long totalElements = supportTicketMapper.countAll();
        return com.hlink.urlshortener.dto.PageResponse.of(tickets, page, size, totalElements);
    }

    /**
     * 사용자별 티켓 페이지네이션 조회
     */
    public com.hlink.urlshortener.dto.PageResponse<SupportTicket> findByUserIdWithPaging(Long userId, int page, int size) {
        int offset = page * size;
        List<SupportTicket> tickets = supportTicketMapper.findByUserIdWithPaging(userId, offset, size);
        long totalElements = supportTicketMapper.countByUserId(userId);
        return com.hlink.urlshortener.dto.PageResponse.of(tickets, page, size, totalElements);
    }

    /**
     * 우선순위별 티켓 페이지네이션 조회
     */
    public com.hlink.urlshortener.dto.PageResponse<SupportTicket> findByPriorityWithPaging(String priority, int page, int size) {
        int offset = page * size;
        List<SupportTicket> tickets = supportTicketMapper.findByPriorityWithPaging(priority, offset, size);
        long totalElements = supportTicketMapper.countByPriority(priority);
        return com.hlink.urlshortener.dto.PageResponse.of(tickets, page, size, totalElements);
    }

    /**
     * 우선순위 값 반환 (숫자로 변환하여 정렬에 사용)
     */
    private int getPriorityValue(String priority) {
        if (priority == null) {
            return 0;
        }
        switch (priority.toUpperCase()) {
            case "HIGH":
                return 4;
            case "MEDIUM":
                return 3;
            case "NORMAL":
                return 2;
            case "LOW":
                return 1;
            default:
                return 0;
        }
    }

    /**
     * 우선순위가 높은 티켓 조회 (HIGH, MEDIUM)
     */
    public List<SupportTicket> findHighPriorityTickets() {
        List<SupportTicket> highPriority = supportTicketMapper.findByPriority("HIGH");
        List<SupportTicket> mediumPriority = supportTicketMapper.findByPriority("MEDIUM");
        highPriority.addAll(mediumPriority);
        highPriority.sort((t1, t2) -> {
            int priority1 = getPriorityValue(t1.getPriority());
            int priority2 = getPriorityValue(t2.getPriority());
            if (priority1 != priority2) {
                return Integer.compare(priority2, priority1);
            }
            return t2.getCreatedAt().compareTo(t1.getCreatedAt());
        });
        return highPriority;
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

    // 댓글 관련 메서드
    public List<TicketComment> getCommentsByTicketId(Long ticketId) {
        return ticketCommentMapper.findByTicketId(ticketId);
    }

    @Transactional
    public TicketComment createComment(Long ticketId, String comment, Long userId) {
        // 티켓 존재 여부 확인
        Optional<SupportTicket> ticketOpt = supportTicketMapper.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("티켓 ID " + ticketId + "가 존재하지 않습니다.");
        }

        // 사용자 존재 여부 확인
        Optional<User> userOpt = userMapper.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("사용자 ID " + userId + "가 존재하지 않습니다.");
        }

        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다");
        }

        TicketComment ticketComment = TicketComment.builder()
                .ticketId(ticketId)
                .userId(userId)
                .comment(comment.trim())
                .createdAt(LocalDateTime.now())
                .build();

        ticketCommentMapper.insert(ticketComment);
        log.info("Comment created for ticket {} by user {}", ticketId, userId);
        return ticketComment;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        TicketComment comment = ticketCommentMapper.findById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("댓글 ID " + commentId + "가 존재하지 않습니다.");
        }
        ticketCommentMapper.deleteById(commentId);
        log.info("Comment deleted: {}", commentId);
    }
}

