package com.hicode.backend.features.inventory.controller;

import com.hicode.backend.features.admin.dto.BloodUnitResponse;
import com.hicode.backend.features.admin.dto.InventorySummary;
import com.hicode.backend.features.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<BloodUnitResponse>> viewInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    // --- CÁC ENDPOINT MỚI ---

    @GetMapping("/summary")
    public ResponseEntity<List<InventorySummary>> getSummary() {
        return ResponseEntity.ok(inventoryService.getInventorySummary());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<BloodUnitResponse>> getRecentAdditions() {
        return ResponseEntity.ok(inventoryService.getRecentAdditions());
    }
}