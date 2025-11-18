# Sample Data Documentation 📦

## Overview
The system automatically creates comprehensive sample data on first startup to help you test all features immediately.

---

## 🏢 Sample Vendors (3)

| Vendor Name | Email | Items Supplied |
|-------------|-------|----------------|
| Office Supplies Co | contact@officesupplies.com | Stationary items |
| Tech Electronics Ltd | sales@techelectronics.com | Electronics |
| Furniture World | info@furnitureworld.com | Furniture |

---

## 📦 Sample Inventory Items (14)

### Stationary (5 items)
| Item | Quantity | Price | Fine Rate | Invoice # |
|------|----------|-------|-----------|-----------|
| Ballpoint Pen | 50 | $0.50 | $0.10 | 1001 |
| Notebook A4 | 30 | $2.50 | $0.25 | 1002 |
| Marker Set | 20 | $5.00 | $0.50 | 1003 |
| Stapler | 15 | $8.00 | $0.75 | 1004 |
| Paper Ream | 8 | $12.00 | $1.00 | 1005 |

### Furniture (4 items)
| Item | Quantity | Price | Fine Rate | Invoice # |
|------|----------|-------|-----------|-----------|
| Office Chair | 12 | $150.00 | $5.00 | 2001 |
| Desk | 8 | $250.00 | $10.00 | 2002 |
| Filing Cabinet | 6 | $180.00 | $7.50 | 2003 |
| Bookshelf | 10 | $120.00 | $5.00 | 2004 |

### Electronics (5 items)
| Item | Quantity | Price | Fine Rate | Invoice # |
|------|----------|-------|-----------|-----------|
| Laptop | 5 | $800.00 | $20.00 | 3001 |
| Monitor | 10 | $200.00 | $10.00 | 3002 |
| Keyboard | 25 | $50.00 | $2.00 | 3003 |
| Mouse | 30 | $25.00 | $1.00 | 3004 |
| Printer | 4 | $300.00 | $15.00 | 3005 |

---

## 👥 Sample Borrowers (4)

| Name | Email |
|------|-------|
| John Doe | john.doe@example.com |
| Jane Smith | jane.smith@example.com |
| Mike Johnson | mike.johnson@example.com |
| Sarah Williams | sarah.williams@example.com |

---

## 📋 Sample Loans (5)

### Currently Borrowed (3 active loans)
| Item | Borrower | Issue Date | Duration | Status |
|------|----------|------------|----------|--------|
| Laptop | John Doe | 2024-11-01 | 7 days | Borrowed |
| Monitor | Jane Smith | 2024-11-05 | 14 days | Borrowed |
| Notebook A4 | Sarah Williams | 2024-11-08 | 7 days | Borrowed |

### Returned Loans (2 completed)
| Item | Borrower | Issue Date | Return Date | Duration | Fine |
|------|----------|------------|-------------|----------|------|
| Office Chair | Mike Johnson | 2024-10-20 | 2024-11-10 | 21 days | $5.00 |
| Laptop | Jane Smith | 2024-10-15 | 2024-10-30 | 15 days | $0.00 |

---

## 🎯 What You Can Test Immediately

### 1. **Item Stock Report** (`/reports/stock`)
- ✅ View 14 different items
- ✅ See stock levels (Low Stock: Paper Ream, Printer)
- ✅ Check item types and prices
- ✅ Test search and sorting

### 2. **Borrow History** (`/reports/borrow-history`)
- ✅ View 5 total loans
- ✅ Filter by date range
- ✅ See returned vs borrowed status
- ✅ Check fine calculations

### 3. **Currently Borrowed Items** (`/reports/borrowed-items`)
- ✅ View 3 active loans
- ✅ See borrower details
- ✅ Check due dates
- ✅ Monitor current fines

### 4. **Issued Items Report** (`/reports/issued-items`)
- ✅ View all 5 issued items
- ✅ Filter by date
- ✅ See status (Borrowed/Returned)
- ✅ Track borrower information

### 5. **Vendor Management** (`/vendors`)
- ✅ View 3 vendors
- ✅ See vendor contact info
- ✅ Add new vendors
- ✅ Delete vendors

### 6. **Item Management** (`/ItemView`)
- ✅ View all 14 items
- ✅ Add new items
- ✅ Edit existing items
- ✅ Delete items
- ✅ See vendor associations

### 7. **Chatbot Queries**
Try these queries:
- "Show me all inventory items"
- "What items are low in stock?"
- "List all vendors"
- "Show me all borrowers"
- "What are the active loans?"

---

## 📊 Sample Data Statistics

- **Total Items**: 14
- **Total Quantity**: 233 units
- **Total Inventory Value**: ~$3,500
- **Low Stock Items**: 2 (Paper Ream: 8, Printer: 4)
- **Active Loans**: 3
- **Completed Loans**: 2
- **Total Borrowers**: 4
- **Total Vendors**: 3

---

## 🔄 Data Initialization Logic

The sample data is created automatically when:
1. ✅ Application starts for the first time
2. ✅ Vendor table is empty
3. ✅ No existing sample data detected

**Note**: Sample data is created only once. If you restart the application, it won't duplicate the data.

---

## 🗑️ Resetting Sample Data

To reset and recreate sample data:

1. **Drop all tables** (or delete database):
```sql
DROP DATABASE IMS;
CREATE DATABASE IMS;
```

2. **Restart application** - Sample data will be recreated automatically

---

## 🎨 Customizing Sample Data

To modify sample data, edit `DataInitializer.java`:

```java
// Add more vendors
Vendor vendor4 = new Vendor();
vendor4.setName("Your Vendor Name");
vendor4.setEmail("email@example.com");
vendorRepository.save(vendor4);

// Add more items
createSampleItem("Item Name", quantity, price, fineRate, 
                 invoiceNumber, vendor, itemType);

// Add more borrowers
Borrower borrower5 = new Borrower();
borrower5.setFirstName("First");
borrower5.setLastName("Last");
borrower5.setEmail("email@example.com");
borrowerRepository.save(borrower5);
```

---

## ✅ Verification Checklist

After application starts, verify:
- [ ] 3 vendors created
- [ ] 14 items created
- [ ] 4 borrowers created
- [ ] 5 loans created
- [ ] Stock report shows all items
- [ ] Borrow history shows 5 loans
- [ ] Currently borrowed shows 3 items
- [ ] Issued items report works
- [ ] Chatbot can query data

---

## 🚀 Quick Test Workflow

1. **Start Application**
   ```bash
   mvnw.cmd spring-boot:run
   ```

2. **Login**
   - URL: http://localhost:8087
   - Username: admin
   - Password: admin123

3. **Test Reports**
   - Sidebar → Reports & Analytics
   - View each report
   - Test filters and search

4. **Test Inventory**
   - Sidebar → Item Management
   - View items
   - Try adding a new item

5. **Test Vendors**
   - Sidebar → Vendor Management
   - View vendors
   - Try adding a new vendor

6. **Test Chatbot**
   - Click chatbot button (bottom-right)
   - Ask: "Show me all inventory items"
   - Ask: "What items are low in stock?"

---

**Your system is now fully populated with realistic sample data for comprehensive testing!** 🎉
