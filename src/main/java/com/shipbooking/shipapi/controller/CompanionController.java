package com.shipbooking.shipapi.controller;

import com.shipbooking.shipapi.dto.CompanionDto;
import com.shipbooking.shipapi.service.CompanionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [동행자 정보 관리 REST API Controller]
 * 동행자 정보 등록, 회원별 조회, 수정, 삭제 엔드포인트 제공
 */
@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/companions")
@RequiredArgsConstructor
public class CompanionController {

    private final CompanionService companionService;

    /**
     * [1. 동행자 정보 등록 API]
     * HTTP Method: POST
     * URL: /api/companions
     *
     * @param request 동행자 등록 요청 데이터 (memberId, companionName, birthDate, gender, nationality, phoneNumber, emergencyContact)
     * @return 저장된 동행자 Response 객체 (companionId 포함)
     */
    @PostMapping
    public ResponseEntity<CompanionDto.Response> createCompanion(@RequestBody CompanionDto.CreateRequest request) {
        log.info("[동행자 API] 등록 요청 - memberId={}, companionName={}", request.getMemberId(), request.getCompanionName());
        CompanionDto.Response response = companionService.createCompanion(request);
        return ResponseEntity.ok(response);
    }

    /**
     * [1-2. 다중 동행자 정보 일괄 등록 API]
     * HTTP Method: POST
     * URL: /api/companions/batch
     *
     * @param requests 동행자 등록 요청 데이터 리스트 (JSON Array [ { memberId, companionName, ... }, { ... } ])
     * @return DB 저장 성공 후 생성된 동행자 Response 객체 리스트
     */
    @PostMapping("/batch")
    public ResponseEntity<List<CompanionDto.Response>> createCompanionsBatch(@RequestBody List<CompanionDto.CreateRequest> requests) {
        log.info("[동행자 API] 다중 일괄 등록 요청 - 수신 건수={}", requests != null ? requests.size() : 0);
        List<CompanionDto.Response> responses = companionService.createCompanionsBatch(requests);
        return ResponseEntity.ok(responses);
    }

    /**
     * [2. 특정 회원의 동행자 목록 조회 API]
     * HTTP Method: GET
     * URL: /api/companions/member/{memberId}
     *
     * @param memberId 동행자를 조회할 회원의 고유 ID (FK)
     * @return 해당 회원이 등록한 동행자 목록 리스트
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<CompanionDto.Response>> getCompanionsByMemberId(@PathVariable("memberId") Long memberId) {
        log.info("[동행자 API] 회원별 목록 조회 - memberId={}", memberId);
        List<CompanionDto.Response> list = companionService.getCompanionsByMemberId(memberId);
        return ResponseEntity.ok(list);
    }

    /**
     * [3. 단일 동행자 정보 상세 조회 API]
     * HTTP Method: GET
     * URL: /api/companions/{companionId}
     *
     * @param companionId 동행자 PK ID
     * @return 동행자 상세 정보 Response 객체
     */
    @GetMapping("/{companionId}")
    public ResponseEntity<CompanionDto.Response> getCompanionById(@PathVariable("companionId") Long companionId) {
        log.info("[동행자 API] 단일 상세 조회 - companionId={}", companionId);
        CompanionDto.Response response = companionService.getCompanionById(companionId);
        return ResponseEntity.ok(response);
    }

    /**
     * [4. 동행자 정보 수정 API]
     * HTTP Method: PUT
     * URL: /api/companions/{companionId}
     *
     * @param companionId 수정할 동행자 PK ID
     * @param request 수정할 항목들 (companionName, birthDate, gender, nationality, phoneNumber, emergencyContact)
     * @return 수정 반영 후의 동행자 Response 객체
     */
    @PutMapping("/{companionId}")
    public ResponseEntity<CompanionDto.Response> updateCompanion(
            @PathVariable("companionId") Long companionId,
            @RequestBody CompanionDto.UpdateRequest request) {
        log.info("[동행자 API] 정보 수정 요청 - companionId={}", companionId);
        CompanionDto.Response response = companionService.updateCompanion(companionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * [5. 동행자 정보 삭제 API]
     * HTTP Method: DELETE
     * URL: /api/companions/{companionId}
     *
     * @param companionId 삭제할 동행자 PK ID
     * @return 200 OK 상태 코드
     */
    @DeleteMapping("/{companionId}")
    public ResponseEntity<Void> deleteCompanion(@PathVariable("companionId") Long companionId) {
        log.info("[동행자 API] 정보 삭제 요청 - companionId={}", companionId);
        companionService.deleteCompanion(companionId);
        return ResponseEntity.ok().build();
    }
}
