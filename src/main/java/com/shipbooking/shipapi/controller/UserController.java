package com.shipbooking.shipapi.controller;

import com.shipbooking.shipapi.dto.UserDto;
import com.shipbooking.shipapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/demo/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto.Response> signUp(@RequestBody UserDto.SignUpRequest request) {
        log.info("[회원 API] 회원가입 요청 - 전화번호(ID): {}, 이름: {}", request.getPhone(), request.getName());
        UserDto.Response response = userService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.Response> login(@RequestBody UserDto.LoginRequest request) {
        log.info("[회원 API] 로그인 요청 - 전화번호(ID): {}", request.getPhone());
        UserDto.Response response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}
