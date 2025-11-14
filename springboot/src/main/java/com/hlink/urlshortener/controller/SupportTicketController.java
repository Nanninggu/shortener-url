package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.CommentCreateRequest;
import com.hlink.urlshortener.dto.SupportTicketCreateRequest;
import com.hlink.urlshortener.model.SupportTicket;
import com.hlink.urlshortener.model.TicketComment;
import com.hlink.urlshortener.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://223.130.157.227:3000", "http://223.130.157.227"})
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping("/tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupportTicket> createTicket(@Valid @RequestBody SupportTicketCreateRequest request,
                                                      @RequestParam Long userId) {
        SupportTicket ticket = supportTicketService.createTicket(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/tickets/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupportTicket> getTicket(@PathVariable Long id) {
        return supportTicketService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tickets/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTicketsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        // 페이징 파라미터가 있으면 페이징된 결과 반환
        if (page >= 0 && size > 0) {
            com.hlink.urlshortener.dto.PageResponse<SupportTicket> pageResponse = supportTicketService.findByUserIdWithPaging(userId, page, size);
            return ResponseEntity.ok(pageResponse);
        }
        // 페이징 파라미터가 없으면 전체 목록 반환 (하위 호환성)
        return ResponseEntity.ok(supportTicketService.findByUserId(userId));
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getAllTickets(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        // 페이징 파라미터가 있으면 페이징된 결과 반환
        if (page >= 0 && size > 0) {
            com.hlink.urlshortener.dto.PageResponse<SupportTicket> pageResponse = supportTicketService.findAllWithPaging(page, size);
            return ResponseEntity.ok(pageResponse);
        }
        // 페이징 파라미터가 없으면 전체 목록 반환 (하위 호환성)
        if ("priority".equals(sort)) {
            return ResponseEntity.ok(supportTicketService.findAllByPriority());
        }
        return ResponseEntity.ok(supportTicketService.findAll());
    }

    @GetMapping("/tickets/priority/{priority}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getTicketsByPriority(
            @PathVariable String priority,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        // 페이징 파라미터가 있으면 페이징된 결과 반환
        if (page >= 0 && size > 0) {
            com.hlink.urlshortener.dto.PageResponse<SupportTicket> pageResponse = supportTicketService.findByPriorityWithPaging(priority, page, size);
            return ResponseEntity.ok(pageResponse);
        }
        // 페이징 파라미터가 없으면 전체 목록 반환 (하위 호환성)
        return ResponseEntity.ok(supportTicketService.findByPriority(priority));
    }

    @GetMapping("/tickets/high-priority")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<SupportTicket>> getHighPriorityTickets() {
        return ResponseEntity.ok(supportTicketService.findHighPriorityTickets());
    }

    @PostMapping("/tickets/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> assignTicket(@PathVariable Long id, @RequestParam Long assignedTo) {
        supportTicketService.assignTicket(id, assignedTo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tickets/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> resolveTicket(@PathVariable Long id) {
        supportTicketService.resolveTicket(id);
        return ResponseEntity.ok().build();
    }

    // 댓글 관련 엔드포인트
    @GetMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketComment>> getComments(@PathVariable Long ticketId) {
        List<TicketComment> comments = supportTicketService.getCommentsByTicketId(ticketId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketComment> createComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CommentCreateRequest request,
            @RequestParam Long userId) {
        TicketComment comment = supportTicketService.createComment(ticketId, request.getComment(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @DeleteMapping("/tickets/{ticketId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId) {
        supportTicketService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}

