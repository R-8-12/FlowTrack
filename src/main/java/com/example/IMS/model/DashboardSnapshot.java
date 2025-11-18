package com.example.IMS.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_snapshot")
public class DashboardSnapshot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "items_borrowed")
    private long itemsBorrowed;
    
    @Column(name = "items_returned")
    private long itemsReturned;
    
    @Column(name = "inventory_remaining")
    private int inventoryRemaining;
    
    @Column(name = "items_issued")
    private long itemsIssued;
    
    @Column(name = "event_type")
    private String eventType; // "ITEM_ADDED", "ITEM_ISSUED", "ITEM_RETURNED", etc.
    
    public DashboardSnapshot() {
    }
    
    public DashboardSnapshot(LocalDateTime timestamp, long itemsBorrowed, long itemsReturned, 
                            int inventoryRemaining, long itemsIssued, String eventType) {
        this.timestamp = timestamp;
        this.itemsBorrowed = itemsBorrowed;
        this.itemsReturned = itemsReturned;
        this.inventoryRemaining = inventoryRemaining;
        this.itemsIssued = itemsIssued;
        this.eventType = eventType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getItemsBorrowed() {
        return itemsBorrowed;
    }

    public void setItemsBorrowed(long itemsBorrowed) {
        this.itemsBorrowed = itemsBorrowed;
    }

    public long getItemsReturned() {
        return itemsReturned;
    }

    public void setItemsReturned(long itemsReturned) {
        this.itemsReturned = itemsReturned;
    }

    public int getInventoryRemaining() {
        return inventoryRemaining;
    }

    public void setInventoryRemaining(int inventoryRemaining) {
        this.inventoryRemaining = inventoryRemaining;
    }

    public long getItemsIssued() {
        return itemsIssued;
    }

    public void setItemsIssued(long itemsIssued) {
        this.itemsIssued = itemsIssued;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
