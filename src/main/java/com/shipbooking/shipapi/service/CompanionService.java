package com.shipbooking.shipapi.service;

import com.shipbooking.shipapi.dto.CompanionDto;
import com.shipbooking.shipapi.entity.Companion;
import com.shipbooking.shipapi.repository.CompanionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [동행자 서비스 비즈니스 로직 Class]
 * 동행자 정보 생성, 회원별 목록 조회, 수정, 삭제 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanionService {

    private final CompanionRepository companionRepository;

    /**
     * [동행자 정보 등록]
     *
     * @param request 프론트엔드에서 보낸 동행자 등록 데이터 (memberId, 이름, 생년월일, 성별, 국적 등)
     * @return DB 저장 성공 후 생성된 동행자 Response DTO
     */
    @Transactional
    public CompanionDto.Response createCompanion(CompanionDto.CreateRequest request) {
        if (request.getMemberId() == null) {
            throw new IllegalArgumentException("동행자를 등록할 대표 회원 번호(memberId)는 필수입니다.");
        }
        if (request.getCompanionName() == null || request.getCompanionName().trim().isEmpty()) {
            throw new IllegalArgumentException("동행자 이름은 필수입니다.");
        }

        Companion companion = Companion.builder()
                .memberId(request.getMemberId())
                .companionName(request.getCompanionName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .phoneNumber(request.getPhoneNumber())
                .emergencyContact(request.getEmergencyContact())
                .build();

        Companion saved = companionRepository.save(companion);
        log.info("[동행자 등록 성공] CompanionID={}, MemberID={}, 이름={}", saved.getId(), saved.getMemberId(), saved.getCompanionName());
        return convertToResponse(saved);
    }

    /**
     * [다중 동행자 정보 일괄 등록 - List<CreateRequest>]
     *
     * @param requests 동행자 등록 데이터 리스트
     * @return DB 저장 성공 후 생성된 동행자 Response DTO 리스트
     */
    @Transactional
    public List<CompanionDto.Response> createCompanionsBatch(List<CompanionDto.CreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("등록할 동행자 정보가 최소 1건 이상이어야 합니다.");
        }

        List<Companion> companions = requests.stream().map(req -> {
            if (req.getMemberId() == null) {
                throw new IllegalArgumentException("동행자를 등록할 대표 회원 번호(memberId)는 필수입니다.");
            }
            if (req.getCompanionName() == null || req.getCompanionName().trim().isEmpty()) {
                throw new IllegalArgumentException("동행자 이름은 필수입니다.");
            }
            return Companion.builder()
                    .memberId(req.getMemberId())
                    .companionName(req.getCompanionName())
                    .birthDate(req.getBirthDate())
                    .gender(req.getGender())
                    .nationality(req.getNationality())
                    .phoneNumber(req.getPhoneNumber())
                    .emergencyContact(req.getEmergencyContact())
                    .build();
        }).collect(Collectors.toList());

        List<Companion> savedList = companionRepository.saveAll(companions);
        log.info("[다중 동행자 일괄 등록 성공] 등록 건수={}", savedList.size());
        return savedList.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * [다중 동행자 정보 일괄 등록 - BatchCreateRequest 객체]
     *
     * @param batchRequest memberId 및 companions 동행자 목록을 포함한 객체
     * @return DB 저장 성공 후 생성된 동행자 Response DTO 리스트
     */
    @Transactional
    public List<CompanionDto.Response> createCompanionsBatchWrapper(CompanionDto.BatchCreateRequest batchRequest) {
        if (batchRequest == null || batchRequest.getCompanions() == null || batchRequest.getCompanions().isEmpty()) {
            throw new IllegalArgumentException("등록할 동행자 정보가 최소 1건 이상이어야 합니다.");
        }

        Long defaultMemberId = batchRequest.getMemberId();
        List<CompanionDto.CreateRequest> list = batchRequest.getCompanions();
        if (defaultMemberId != null) {
            for (CompanionDto.CreateRequest req : list) {
                if (req.getMemberId() == null) {
                    req.setMemberId(defaultMemberId);
                }
            }
        }
        return createCompanionsBatch(list);
    }

    /**
     * [특정 회원의 동행자 목록 조회]
     *
     * @param memberId 회원 고유 ID (FK)
     * @return 회원이 등록한 전체 동행자 리스트
     */
    @Transactional(readOnly = true)
    public List<CompanionDto.Response> getCompanionsByMemberId(Long memberId) {
        List<Companion> companions = companionRepository.findByMemberId(memberId);
        log.info("[동행자 목록 조회] MemberID={}, 조회 수={}", memberId, companions.size());
        return companions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * [단일 동행자 정보 상세 조회]
     *
     * @param companionId 동행자 PK ID
     * @return 동행자 상세 정보 Response DTO
     */
    @Transactional(readOnly = true)
    public CompanionDto.Response getCompanionById(Long companionId) {
        Companion companion = companionRepository.findById(companionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동행자 정보입니다: " + companionId));
        return convertToResponse(companion);
    }

    /**
     * [동행자 정보 수정]
     *
     * @param companionId 수정할 동행자 PK ID
     * @param request 수정할 필드들이 담긴 UpdateRequest DTO
     * @return 수정 반영 후의 동행자 Response DTO
     */
    @Transactional
    public CompanionDto.Response updateCompanion(Long companionId, CompanionDto.UpdateRequest request) {
        Companion companion = companionRepository.findById(companionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동행자 정보입니다: " + companionId));

        if (request.getCompanionName() != null) companion.setCompanionName(request.getCompanionName());
        if (request.getBirthDate() != null) companion.setBirthDate(request.getBirthDate());
        if (request.getGender() != null) companion.setGender(request.getGender());
        if (request.getNationality() != null) companion.setNationality(request.getNationality());
        if (request.getPhoneNumber() != null) companion.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmergencyContact() != null) companion.setEmergencyContact(request.getEmergencyContact());

        log.info("[동행자 수정 성공] CompanionID={}, 수정된 이름={}", companion.getId(), companion.getCompanionName());
        return convertToResponse(companion);
    }

    /**
     * [동행자 정보 삭제]
     *
     * @param companionId 삭제할 동행자 PK ID
     */
    @Transactional
    public void deleteCompanion(Long companionId) {
        Companion companion = companionRepository.findById(companionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동행자 정보입니다: " + companionId));
        companionRepository.delete(companion);
        log.info("[동행자 삭제 성공] CompanionID={}", companionId);
    }

    private CompanionDto.Response convertToResponse(Companion companion) {
        return CompanionDto.Response.builder()
                .companionId(companion.getId())
                .memberId(companion.getMemberId())
                .companionName(companion.getCompanionName())
                .birthDate(companion.getBirthDate())
                .gender(companion.getGender())
                .nationality(companion.getNationality())
                .phoneNumber(companion.getPhoneNumber())
                .emergencyContact(companion.getEmergencyContact())
                .createdAt(companion.getCreatedAt())
                .updatedAt(companion.getUpdatedAt())
                .build();
    }
}
