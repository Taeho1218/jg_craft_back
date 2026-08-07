package com.shipbooking.shipapi.controller;

import com.shipbooking.shipapi.dto.UserDto;
import com.shipbooking.shipapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j // 로깅(로그 출력) 기능을 사용하기 위한 Lombok 어노테이션
@CrossOrigin(origins = "*") // 프론트엔드(다른 포트/도메인)에서 보내는 CORS 요청을 허용
@RestController // JSON 형태 데이터를 주고받는 RESTful API 전용 컨트롤러 선언
@RequestMapping("/api/users") // 이 클래스의 모든 API 경로의 기본 주소 Prefix
@RequiredArgsConstructor // final 필드인 userService의 생성자를 자동 생성하여 스프링이 주입(DI)
public class UserController {

    private final UserService userService; // 실제 회원가입/로그인 비즈니스 로직을 처리하는 서비스

    /**
     * [회원가입 API Endpoint]
     * HTTP Method: POST
     *
     * @param request 프론트엔드에서 JSON 형태로 보낸 회원가입 데이터 (전화번호, 이메일, 비밀번호, 이름 등)
     * @return 200 OK 상태 코드와 함께 저장된 회원 데이터 반환
     */
    @PostMapping("/signup")
    public ResponseEntity<UserDto.Response> signUp(@RequestBody UserDto.SignUpRequest request) {
        log.info("[회원 API] 회원가입 요청 - 전화번호(ID): {}, 이름: {}", request.getPhone(), request.getName());

        // 서비스 계층의 signUp 메서드를 호출하여 실제 회원가입 처리 수행
        UserDto.Response response = userService.signUp(request);

        // HTTP 상태 코드 200(OK)과 함께 결과 JSON 응답
        return ResponseEntity.ok(response);
    }

    /**
     * [로그인 API Endpoint]
     * HTTP Method: POST
     *
     * @param request 프론트엔드에서 JSON 형태로 보낸 로그인 데이터 (전화번호, 비밀번호)
     * @return 200 OK 상태 코드와 함께 로그인 성공한 회원 데이터 반환
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto.Response> login(@RequestBody UserDto.LoginRequest request) {
        log.info("[회원 API] 로그인 요청 - 전화번호(ID): {}", request.getPhone());

        // 서비스 계층의 login 메서드를 호출하여 비밀번호 검증 및 회원이 존재하는지 확인
        UserDto.Response response = userService.login(request);

        // HTTP 상태 코드 200(OK)과 함께 로그인된 회원 정보 반환
        return ResponseEntity.ok(response);
    }

    /**
     * [회원 정보 조회 API Endpoint]
     * HTTP Method: GET
     * 예매 화면에서 내 정보를 자동으로 불러올 때 사용
     *
     * @param id 조회할 회원의 고유 ID (PK)
     * @return 200 OK 상태 코드와 함께 회원 정보 반환
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> getUser(@PathVariable Long id) {
        log.info("[회원 API] 회원 정보 조회 요청 - 회원ID: {}", id);

        // 서비스 계층의 getUserById 메서드를 호출하여 회원 정보 조회
        UserDto.Response response = userService.getUserById(id);

        // HTTP 상태 코드 200(OK)과 함께 회원 정보 반환
        return ResponseEntity.ok(response);
    }

    /**
     * [전화번호(로그인 ID)로 회원 정보 조회 API Endpoint]
     * HTTP Method: GET
     * URL: /api/users/phone/{phone}
     *
     * @param phone 조회할 회원의 전화번호 (예: 01012345678)
     * @return 200 OK 상태 코드와 함께 회원 정보 반환
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<UserDto.Response> getUserByPhone(@PathVariable String phone) {
        log.info("[회원 API] 전화번호로 회원 정보 조회 요청 - phone: {}", phone);
        UserDto.Response response = userService.getUserByPhone(phone);
        return ResponseEntity.ok(response);
    }

    /**
     * [비밀번호 검증 API Endpoint]
     * HTTP Method: POST
     * 개인정보 수정 전 본인 확인용으로 사용
     *
     * @param id      검증할 회원의 고유 ID (PK)
     * @param request 검증할 비밀번호
     * @return 200 OK (일치) / 예외 발생 (불일치)
     */
    @PostMapping("/{id}/verify-password")
    public ResponseEntity<Void> verifyPassword(
            @PathVariable Long id,
            @RequestBody UserDto.VerifyPasswordRequest request) {
        log.info("[회원 API] 비밀번호 검증 요청 - 회원ID: {}", id);

        // 서비스 계층의 verifyPassword 메서드를 호출하여 비밀번호 일치 여부 검증
        userService.verifyPassword(id, request.getPassword());

        // 비밀번호 일치 시 HTTP 상태 코드 200(OK) 반환 (응답 body 없음)
        return ResponseEntity.ok().build();
    }

    /**
     * [회원 탈퇴 API Endpoint]
     * HTTP Method: DELETE
     * 비밀번호 검증(POST /{id}/verify-password) 후 호출
     * 실제 삭제 없이 탈퇴 상태로만 변경 (소프트 딜리트)
     *
     * @param id 탈퇴할 회원의 고유 ID (PK)
     * @return 200 OK (응답 body 없음)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("[회원 API] 회원 탈퇴 요청 - 회원ID: {}", id);

        // 서비스 계층의 deleteUser 메서드를 호출하여 탈퇴 처리 (deleted = true)
        userService.deleteUser(id);

        // HTTP 상태 코드 200(OK) 반환 (응답 body 없음)
        return ResponseEntity.ok().build();
    }

    /**
     * [개인정보 수정 API Endpoint]
     * HTTP Method: PATCH
     * 비밀번호 검증 후 개인정보 수정 시 사용
     * 필수: 휴대폰 번호, 이름, 생년월일 / 선택: 나머지 항목
     *
     * @param id      수정할 회원의 고유 ID (PK)
     * @param request 수정할 회원 정보
     * @return 200 OK 상태 코드와 함께 수정된 회원 정보 반환
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserDto.Response> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto.UpdateRequest request) {
        log.info("[회원 API] 개인정보 수정 요청 - 회원ID: {}", id);

        // 서비스 계층의 updateUser 메서드를 호출하여 회원 정보 수정 처리
        UserDto.Response response = userService.updateUser(id, request);

        // HTTP 상태 코드 200(OK)과 함께 수정된 회원 정보 반환
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[회원 API 예외 발생] {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
