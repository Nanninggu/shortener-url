package com.hlink.urlshortener.controller;

import com.hlink.urlshortener.dto.SupportTicketCreateRequest;
import com.hlink.urlshortener.model.SupportTicket;
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
@CrossOrigin(origins = "http://localhost:3000")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping("/tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupportTicket> createTicket(@Valid @RequestBody SupportTicketCreateRequest request,
                                                      @RequestParam Long userId) {
        try {
            SupportTicket ticket = supportTicketService.createTicket(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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
    public ResponseEntity<List<SupportTicket>> getTicketsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(supportTicketService.findByUserId(userId));
    }

    @GetMapping("/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupportTicket>> getAllTickets() {
        return ResponseEntity.ok(supportTicketService.findAll());
    }

    @PostMapping("/tickets/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignTicket(@PathVariable Long id, @RequestParam Long assignedTo) {
        supportTicketService.assignTicket(id, assignedTo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tickets/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resolveTicket(@PathVariable Long id) {
        supportTicketService.resolveTicket(id);
        return ResponseEntity.ok().build();
    }
}

