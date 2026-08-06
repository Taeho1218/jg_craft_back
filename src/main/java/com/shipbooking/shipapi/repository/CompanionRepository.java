package com.shipbooking.shipapi.repository;

import com.shipbooking.shipapi.entity.Companion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * [동행자 리포지토리 인터페이스]
 * 동행자 테이블(companion) CRUD 및 회원별 조회 쿼리 제공
 */
public interface CompanionRepository extends JpaRepository<Companion, Long> {

    /**
     * [회원 PK ID별 동행자 목록 조회]
     *
     * @param memberId 동행자를 등록한 회원 ID (FK)
     * @return 해당 회원이 등록한 동행자 엔티티 리스트
     */
    List<Companion> findByMemberId(Long memberId);
}
