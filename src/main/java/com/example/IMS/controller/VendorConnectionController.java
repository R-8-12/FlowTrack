package com.example.IMS.controller;

import com.example.IMS.dto.ConnectionResponseDTO;
import com.example.IMS.dto.VendorActionDTO;
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

/**
 * REST API for vendor-side connection management.
 *
 * <p>All endpoints require ROLE_VENDOR.
 */
@RestController
@RequestMapping("/api/vendor/connections")
@PreAuthorize("hasAuthority('ROLE_VENDOR')")
public class VendorConnectionController {

    @Autowired
    private VendorConnectionService connectionService;

    /** GET /api/vendor/connections/pending — pending requests awaiting approval */
    @GetMapping("/pending")
    public ResponseEntity<List<ConnectionResponseDTO>> pendingRequests() {
        return ResponseEntity.ok(connectionService.getPendingRequestsForVendor(currentUserId()));
    }

    /** GET /api/vendor/connections/connected — approved retailer connections */
    @GetMapping("/connected")
    public ResponseEntity<List<ConnectionResponseDTO>> connectedRetailers() {
        return ResponseEntity.ok(connectionService.getConnectedRetailersForVendor(currentUserId()));
    }

    /** POST /api/vendor/connections/respond — approve or reject a request */
    @PostMapping("/respond")
    public ResponseEntity<ConnectionResponseDTO> respond(@Valid @RequestBody VendorActionDTO action) {
        return ResponseEntity.ok(connectionService.respondToConnection(currentUserId(), action));
    }

    /** POST /api/vendor/connections/{id}/block */
    @PostMapping("/{id}/block")
    public ResponseEntity<ConnectionResponseDTO> block(@PathVariable Long id) {
        return ResponseEntity.ok(connectionService.blockConnection(currentUserId(), id));
    }

    /** POST /api/vendor/connections/{id}/unblock */
    @PostMapping("/{id}/unblock")
    public ResponseEntity<ConnectionResponseDTO> unblock(@PathVariable Long id) {
        return ResponseEntity.ok(connectionService.unblockConnection(currentUserId(), id));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((User) auth.getPrincipal()).getId();
    }
}
