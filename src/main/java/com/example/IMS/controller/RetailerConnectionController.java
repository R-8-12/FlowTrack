package com.example.IMS.controller;

import com.example.IMS.dto.ConnectionRequestDTO;
import com.example.IMS.dto.ConnectionResponseDTO;
import com.example.IMS.model.User;
import com.example.IMS.service.VendorConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST API for retailer-side connection management.
 *
 * <p>All endpoints require ROLE_RETAILER.
 */
@RestController
@RequestMapping("/api/retailer/connections")
@PreAuthorize("hasAuthority('ROLE_RETAILER')")
public class RetailerConnectionController {

    @Autowired
    private VendorConnectionService connectionService;

    /** POST /api/retailer/connections — request a new connection */
    @PostMapping
    public ResponseEntity<ConnectionResponseDTO> requestConnection(
            @Valid @RequestBody ConnectionRequestDTO request) {
        Long userId = currentUserId();
        ConnectionResponseDTO response = connectionService.requestConnection(userId, request);
        return ResponseEntity.ok(response);
    }

    /** GET /api/retailer/connections — list all connections for this retailer */
    @GetMapping
    public ResponseEntity<List<ConnectionResponseDTO>> listConnections() {
        return ResponseEntity.ok(connectionService.getRetailerConnections(currentUserId()));
    }

    /** GET /api/retailer/connections/status?vendorProfileId=X — check status with a specific vendor */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestParam Long vendorProfileId) {
        ConnectionResponseDTO dto = connectionService.getConnectionStatus(currentUserId(), vendorProfileId);
        if (dto == null) {
            return ResponseEntity.ok(Map.of("status", "NONE"));
        }
        return ResponseEntity.ok(dto);
    }

    /** POST /api/retailer/connections/{id}/block */
    @PostMapping("/{id}/block")
    public ResponseEntity<ConnectionResponseDTO> block(@PathVariable Long id) {
        return ResponseEntity.ok(connectionService.blockConnection(currentUserId(), id));
    }

    /** POST /api/retailer/connections/{id}/unblock */
    @PostMapping("/{id}/unblock")
    public ResponseEntity<ConnectionResponseDTO> unblock(@PathVariable Long id) {
        return ResponseEntity.ok(connectionService.unblockConnection(currentUserId(), id));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((User) auth.getPrincipal()).getId();
    }
}
