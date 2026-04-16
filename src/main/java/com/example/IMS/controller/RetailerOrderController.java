package com.example.IMS.controller;

import com.example.IMS.dto.ItemDto;
import com.example.IMS.model.ProcurementOrder;
import com.example.IMS.model.User;
import com.example.IMS.service.ItemTypeService;
import com.example.IMS.service.ProcurementOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/retailer/orders")
public class RetailerOrderController {

    @Autowired
    private ProcurementOrderService procurementOrderService;

    @Autowired
    private ItemTypeService itemTypeService;

    @GetMapping
    public String listRetailerOrders(Model model) {
        List<ProcurementOrder> orders = procurementOrderService.getRetailerOrders(currentUser());
        model.addAttribute("orders", orders);
        return "retailer/order-requests";
    }

    @PostMapping("/request")
    public String createSupplierRequest(@Valid @ModelAttribute("itemDto") ItemDto itemDto,
                                        BindingResult result,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("itemTypeList", itemTypeService.getAllItemTypes());
            return "retailer/item-create";
        }

        try {
            ProcurementOrder order = procurementOrderService.createOrderRequest(currentUser(), itemDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Supplier order #" + order.getId() + " sent successfully.");
            return "redirect:/retailer/orders";
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("global", ex.getMessage()));
            model.addAttribute("itemTypeList", itemTypeService.getAllItemTypes());
            return "retailer/item-create";
        }
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
