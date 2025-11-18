package com.example.IMS.service;

import java.util.List;
import com.example.IMS.model.Vendor;

public interface IVendorService {
	Vendor getVendorById(long id);

	Vendor getVendorByName(String name);
	
	List<Vendor> getAllVendors();

	String validateVendorId(long id);
	
	String validateVendorName(String vendorName);
}
