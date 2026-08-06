package com.shipbooking.shipapi.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class UserDto {

    @Data
    public static class SignUpRequest {
        private String phone;          // 회원 로그인 ID로 사용
        private String email;          // 이메일
        private String password;       // 비밀번호
        private String name;           // 이름
        private String gender;         // 성별 (M / F)
        private String nationality;    // 국적 (내국인: KOR, 외국인 등)
        private String emergencyPhone; // 비상 연락처
        private String birthDate;      // 생년월일 (YYYY-MM-DD)
    }

    @Data
    public static class LoginRequest {
        private String phone;          // 로그인 ID (전화번호)
        private String password;       // 비밀번호
    }

    @Data
    public static class Response {
        private Long id;
        private String phone;          // 로그인 ID
        private String email;
        private String name;
        private String gender;
        private String nationality;
        private String emergencyPhone;
        private String birthDate;
        private LocalDateTime createdAt;
    }

    @Data
    public static class VerifyPasswordRequest {
        private String password;       // 검증할 비밀번호
    }

    @Data
    public static class UpdateRequest {
        private String phone;          // 휴대폰 번호 (필수)
        private String password;       // 비밀번호 (선택 - 비어있으면 기존 유지)
        private String name;           // 이름 (필수)
        private String birthDate;      // 생년월일 (필수, YYYY-MM-DD)
        private String gender;         // 성별 (선택, M / F)
        private String nationality;    // 국적 (선택, 내국인: KOR 등)
        private String email;          // 이메일 (선택)
        private String emergencyPhone; // 비상 연락처 (선택)
    }
}
