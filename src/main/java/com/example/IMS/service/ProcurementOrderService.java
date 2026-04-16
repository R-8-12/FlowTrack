package com.example.IMS.service;

import com.example.IMS.dto.ItemDto;
import com.example.IMS.model.Item;
import com.example.IMS.model.ItemType;
import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.ProcurementOrderStatus;
import com.example.IMS.model.User;
import com.example.IMS.model.Vendor;
import com.example.IMS.repository.ProcurementOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProcurementOrderService {

    @Autowired
    private ProcurementOrderRepository procurementOrderRepository;

    @Autowired
    private VendorService vendorService;

    @Autowired
    private ItemTypeService itemTypeService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public ProcurementOrder createOrderRequest(User retailer, ItemDto itemDto) {
        String vendorErr = vendorService.validateVendorName(itemDto.getVendorName());
        if (!vendorErr.isEmpty()) {
            throw new RuntimeException(vendorErr);
        }

        String itemTypeErr = itemTypeService.validateItemTypeByName(itemDto.getItemType());
        if (!itemTypeErr.isEmpty()) {
            throw new RuntimeException(itemTypeErr);
        }

        Vendor vendor = vendorService.getVendorByName(itemDto.getVendorName());
        if (vendor == null) {
            throw new RuntimeException("Supplier not found");
        }

        ProcurementOrder order = new ProcurementOrder();
        order.setRetailer(retailer);
        order.setVendor(vendor);
        order.setItemName(itemDto.getItemName());
        order.setItemTypeName(itemDto.getItemType());
        order.setRequestedQuantity(itemDto.getItemQuantity());
        order.setExpectedUnitPrice(itemDto.getItemPrice());
        order.setExpectedFineRate(itemDto.getFineRate());
        order.setRequestedInvoiceNumber(itemDto.getInvoiceNumber());
        order.setRetailerNotes("Requested from Add Item workflow");
        order.setStatus(ProcurementOrderStatus.REQUESTED);

        ProcurementOrder saved = procurementOrderRepository.save(order);

        if (vendor.getEmail() != null && !vendor.getEmail().isBlank()) {
            try {
                emailService.sendSupplierOrderRequestEmail(
                        vendor.getEmail(),
                        vendor.getName(),
                        displayName(retailer),
                        saved.getId(),
                        saved.getItemName(),
                        saved.getRequestedQuantity(),
                        saved.getExpectedUnitPrice(),
                        saved.getRetailerNotes());
            } catch (Exception ignored) {
                // keep ordering flow non-blocking if email fails
            }
        }

        return saved;
    }

    public List<ProcurementOrder> getRetailerOrders(User retailer) {
        return procurementOrderRepository.findByRetailerOrderByCreatedAtDesc(retailer);
    }

    public List<ProcurementOrder> getVendorOrders(Vendor vendor) {
        return procurementOrderRepository.findByVendorOrderByCreatedAtDesc(vendor);
    }

    @Transactional
    public ProcurementOrder updateOrderStatusForVendor(Long orderId, Vendor actingVendor,
                                                       ProcurementOrderStatus newStatus, String vendorNotes) {
        ProcurementOrder order = procurementOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getVendor() == null || order.getVendor().getId() != actingVendor.getId()) {
            throw new RuntimeException("You are not allowed to update this order");
        }

        if (order.getStatus() == ProcurementOrderStatus.REJECTED ||
                order.getStatus() == ProcurementOrderStatus.SUPPLIED) {
            throw new RuntimeException("This order is already closed");
        }

        validateTransition(order.getStatus(), newStatus);

        order.setVendorNotes(vendorNotes != null ? vendorNotes.trim() : null);
        order.setStatus(newStatus);

        if (newStatus == ProcurementOrderStatus.SUPPLIED) {
            applySupplyToInventory(order);
            order.setSuppliedAt(LocalDateTime.now());
        }

        ProcurementOrder saved = procurementOrderRepository.save(order);

        if (saved.getRetailer() != null && saved.getRetailer().getEmail() != null &&
                !saved.getRetailer().getEmail().isBlank()) {
            try {
                emailService.sendSupplierOrderStatusEmail(
                        saved.getRetailer().getEmail(),
                        displayName(saved.getRetailer()),
                        saved.getId(),
                        saved.getItemName(),
                        saved.getStatus().name(),
                        actingVendor.getName(),
                        saved.getVendorNotes());
            } catch (Exception ignored) {
                // keep status update non-blocking if email fails
            }
        }

        return saved;
    }

    private void validateTransition(ProcurementOrderStatus current, ProcurementOrderStatus next) {
        if (next == ProcurementOrderStatus.REQUESTED) {
            throw new RuntimeException("Invalid status transition");
        }

        if (current == ProcurementOrderStatus.REQUESTED &&
                !(next == ProcurementOrderStatus.ACCEPTED || next == ProcurementOrderStatus.REJECTED)) {
            throw new RuntimeException("Only ACCEPTED/REJECTED is allowed from REQUESTED");
        }

        if (current == ProcurementOrderStatus.ACCEPTED &&
                !(next == ProcurementOrderStatus.SUPPLIED || next == ProcurementOrderStatus.REJECTED)) {
            throw new RuntimeException("Only SUPPLIED/REJECTED is allowed from ACCEPTED");
        }
    }

    private void applySupplyToInventory(ProcurementOrder order) {
        ItemType itemType = itemTypeService.getItemTypeByName(order.getItemTypeName());
        if (itemType == null) {
            throw new RuntimeException("Item type not found during supply: " + order.getItemTypeName());
        }

        Optional<Item> existing = itemService.findByNameAndType(order.getItemName(), order.getItemTypeName());
        Item item = existing.orElseGet(Item::new);

        item.setName(order.getItemName());
        item.setItemType(itemType);
        item.setVendor(order.getVendor());

        if (existing.isPresent()) {
            item.setQuantity(item.getQuantity() + order.getRequestedQuantity());
        } else {
            item.setQuantity(order.getRequestedQuantity());
        }

        item.setPrice(order.getExpectedUnitPrice());
        item.setFineRate(order.getExpectedFineRate());
        item.setInvoiceNumber(resolveInvoiceNumber(order));

        itemService.saveItem(item);
    }

    private long resolveInvoiceNumber(ProcurementOrder order) {
        if (order.getRequestedInvoiceNumber() != null && order.getRequestedInvoiceNumber() > 0) {
            return order.getRequestedInvoiceNumber();
        }
        return System.currentTimeMillis() / 1000;
    }

    private String displayName(User user) {
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? user.getUsername() : full;
    }
}
