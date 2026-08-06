package com.shipbooking.shipapi.controller;

import com.shipbooking.shipapi.dto.AdminDto;
import com.shipbooking.shipapi.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * [선사 전달용 승선객 명단 추출 API (출발 1일 전 오전에 선사에 전달할 명단)]
     * GET /api/admin/manifest?scheduleId=1
     */
    @GetMapping("/manifest")
    public ResponseEntity<AdminDto.ManifestResponse> getPassengerManifest(@RequestParam Long scheduleId) {
        log.info("[관리자 API] 선사 제출용 승선객 명단 추출 요청 - 운항 일정 ID: {}", scheduleId);
        AdminDto.ManifestResponse response = adminService.getPassengerManifest(scheduleId);
        return ResponseEntity.ok(response);
    }
}
