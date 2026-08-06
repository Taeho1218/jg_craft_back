package com.shipbooking.shipapi.service;

import com.shipbooking.shipapi.dto.UserDto;
import com.shipbooking.shipapi.entity.User;
import com.shipbooking.shipapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto.Response signUp(UserDto.SignUpRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("이미 등록된 전화번호(로그인 ID)입니다: " + request.getPhone());
        }

        User user = User.builder()
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .gender(request.getGender())
                .nationality(request.getNationality() != null ? request.getNationality() : "KOR")
                .emergencyPhone(request.getEmergencyPhone())
                .birthDate(request.getBirthDate())
                .build();

        User saved = userRepository.save(user);
        log.info("[회원가입 완료] 전화번호(ID)={}, 이름={}, 생년월일={}", saved.getPhone(), saved.getName(), saved.getBirthDate());

        return convertToResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserDto.Response login(UserDto.LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전화번호(ID)입니다: " + request.getPhone()));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("[로그인 성공] 전화번호(ID)={}, 이름={}", user.getPhone(), user.getName());
        return convertToResponse(user);
    }

    private UserDto.Response convertToResponse(User user) {
        UserDto.Response res = new UserDto.Response();
        res.setId(user.getId());
        res.setPhone(user.getPhone());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setGender(user.getGender());
        res.setNationality(user.getNationality());
        res.setEmergencyPhone(user.getEmergencyPhone());
        res.setBirthDate(user.getBirthDate());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }
}
