package com.example.IMS.service;

import com.example.IMS.dto.ConnectionRequestDTO;
import com.example.IMS.dto.ConnectionResponseDTO;
import com.example.IMS.dto.VendorActionDTO;
import com.example.IMS.model.BusinessProfile;
import com.example.IMS.model.RetailerVendorConnection;
import com.example.IMS.model.User;
import com.example.IMS.model.enums.ConnectionStatus;
import com.example.IMS.repository.BusinessProfileRepository;
import com.example.IMS.repository.IUserRepository;
import com.example.IMS.repository.RetailerVendorConnectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VendorConnectionServiceImpl implements VendorConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(VendorConnectionServiceImpl.class);

    @Autowired
    private RetailerVendorConnectionRepository connectionRepository;

    @Autowired
    private BusinessProfileRepository businessProfileRepository;

    @Autowired
    private IUserRepository userRepository;

    // ── Retailer actions ──────────────────────────────────────────────────────

    @Override
    public ConnectionResponseDTO requestConnection(Long retailerUserId, ConnectionRequestDTO request) {
        // Prevent duplicate requests
        if (connectionRepository.existsByRetailerIdAndVendorProfileId(retailerUserId, request.getVendorProfileId())) {
            RetailerVendorConnection existing = connectionRepository
                    .findByRetailerIdAndVendorProfileId(retailerUserId, request.getVendorProfileId())
                    .orElseThrow();
            logger.info("Connection already exists (id={}, status={}) for retailer={} vendor={}",
                    existing.getId(), existing.getStatus(), retailerUserId, request.getVendorProfileId());
            return ConnectionResponseDTO.from(existing);
        }

        User retailer = userRepository.findById(retailerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Retailer not found: " + retailerUserId));

        BusinessProfile vendorProfile = businessProfileRepository.findById(request.getVendorProfileId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor profile not found: " + request.getVendorProfileId()));

        RetailerVendorConnection connection = new RetailerVendorConnection();
        connection.setRetailer(retailer);
        connection.setVendorProfile(vendorProfile);
        connection.setStatus(ConnectionStatus.REQUESTED);
        connection.setRetailerMessage(request.getMessage());

        connection = connectionRepository.save(connection);
        logger.info("Connection requested: id={}, retailer={}, vendor={}", connection.getId(), retailerUserId, request.getVendorProfileId());
        return ConnectionResponseDTO.from(connection);
    }

    // ── Vendor actions ────────────────────────────────────────────────────────

    @Override
    public ConnectionResponseDTO respondToConnection(Long vendorUserId, VendorActionDTO action) {
        RetailerVendorConnection connection = connectionRepository.findById(action.getConnectionId())
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + action.getConnectionId()));

        // Verify the vendor owns this profile
        assertVendorOwnsProfile(vendorUserId, connection.getVendorProfile().getId());

        if (connection.getStatus() != ConnectionStatus.REQUESTED) {
            throw new IllegalStateException("Connection is not in REQUESTED state: " + connection.getStatus());
        }

        connection.setStatus(action.getApprove() ? ConnectionStatus.CONNECTED : ConnectionStatus.REJECTED);
        connection.setVendorResponseNote(action.getNote());

        connection = connectionRepository.save(connection);
        logger.info("Connection {} by vendor={}: id={}", action.getApprove() ? "APPROVED" : "REJECTED", vendorUserId, connection.getId());
        return ConnectionResponseDTO.from(connection);
    }

    @Override
    public ConnectionResponseDTO blockConnection(Long actingUserId, Long connectionId) {
        RetailerVendorConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));

        assertParticipant(actingUserId, connection);

        if (connection.getStatus() != ConnectionStatus.CONNECTED) {
            throw new IllegalStateException("Only CONNECTED connections can be blocked");
        }

        connection.setStatus(ConnectionStatus.BLOCKED);
        connection = connectionRepository.save(connection);
        logger.info("Connection BLOCKED by user={}: id={}", actingUserId, connectionId);
        return ConnectionResponseDTO.from(connection);
    }

    @Override
    public ConnectionResponseDTO unblockConnection(Long actingUserId, Long connectionId) {
        RetailerVendorConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));

        assertParticipant(actingUserId, connection);

        if (connection.getStatus() != ConnectionStatus.BLOCKED) {
            throw new IllegalStateException("Only BLOCKED connections can be unblocked");
        }

        connection.setStatus(ConnectionStatus.CONNECTED);
        connection = connectionRepository.save(connection);
        logger.info("Connection UNBLOCKED by user={}: id={}", actingUserId, connectionId);
        return ConnectionResponseDTO.from(connection);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ConnectionResponseDTO getConnectionStatus(Long retailerUserId, Long vendorProfileId) {
        return connectionRepository
                .findByRetailerIdAndVendorProfileId(retailerUserId, vendorProfileId)
                .map(ConnectionResponseDTO::from)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionResponseDTO> getRetailerConnections(Long retailerUserId) {
        return connectionRepository.findByRetailerId(retailerUserId)
                .stream().map(ConnectionResponseDTO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionResponseDTO> getPendingRequestsForVendor(Long vendorUserId) {
        List<BusinessProfile> profiles = businessProfileRepository.findByUserId(vendorUserId);
        if (profiles.isEmpty()) return List.of();
        Long profileId = profiles.get(0).getId();
        return connectionRepository.findPendingByVendorProfileId(profileId)
                .stream().map(ConnectionResponseDTO::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionResponseDTO> getConnectedRetailersForVendor(Long vendorUserId) {
        List<BusinessProfile> profiles = businessProfileRepository.findByUserId(vendorUserId);
        if (profiles.isEmpty()) return List.of();
        Long profileId = profiles.get(0).getId();
        return connectionRepository.findConnectedByVendorProfileId(profileId)
                .stream().map(ConnectionResponseDTO::from).collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void assertVendorOwnsProfile(Long vendorUserId, Long vendorProfileId) {
        businessProfileRepository.findByIdAndUserId(vendorProfileId, vendorUserId)
                .orElseThrow(() -> new SecurityException("Vendor does not own profile: " + vendorProfileId));
    }

    private void assertParticipant(Long userId, RetailerVendorConnection connection) {
        boolean isRetailer = connection.getRetailer().getId().equals(userId);
        boolean isVendor   = connection.getVendorProfile().getUser().getId().equals(userId);
        if (!isRetailer && !isVendor) {
            throw new SecurityException("User " + userId + " is not a participant in connection " + connection.getId());
        }
    }
}
