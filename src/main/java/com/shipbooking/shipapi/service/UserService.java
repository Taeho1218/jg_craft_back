package com.shipbooking.shipapi.service;

import com.shipbooking.shipapi.dto.UserDto;
import com.shipbooking.shipapi.entity.User;
import com.shipbooking.shipapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j // 로깅(로그 출력) 기능을 자동으로 생성해주는 Lombok 어노테이션
@Service // 스프링이 비즈니스 로직을 처리하는 서비스 클래스로 인식하게 등록
@RequiredArgsConstructor // final이 붙은 필드(userRepository)의 생성자를 자동으로 만들어 의존성 주입(DI)
public class UserService {

    private final UserRepository userRepository; // DB 작업을 담당하는 리포지토리

    /**
     * [회원가입 기능]
     * 1. 전화번호(로그인 ID) 중복 검사
     * 2. 회원 데이터(User Entity) 객체 생성 및 조립
     * 3. DB에 회원 정보 저장 (save)
     * 4. 저장된 결과를 응답 데이터(Response DTO)로 변환 후 반환
     */
    @Transactional // 메서드 실행 중 예외(에러)가 발생하면 DB 변경 사항을 자동으로 롤백(취소)함
    public UserDto.Response signUp(UserDto.SignUpRequest request) {
        // [1단계: 전화번호 중복 확인] 이미 가입된 전화번호인 경우 예외 발생
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("이미 등록된 전화번호(로그인 ID)입니다: " + request.getPhone());
        }

        String email = (request.getEmail() != null && !request.getEmail().isBlank()) ? request.getEmail() : null;
        String emergencyPhone = (request.getEmergencyPhone() != null && !request.getEmergencyPhone().isBlank()) ? request.getEmergencyPhone() : null;
        if (emergencyPhone != null && emergencyPhone.equals(request.getPhone())) {
            emergencyPhone = null;
        }

        // [2단계: 프론트엔드가 보낸 데이터(request)를 DB 엔티티 객체로 변환]
        User user = User.builder()
                .phone(request.getPhone())                 // 전화번호 (로그인 ID로 사용)
                .email(email)                              // 이메일 (빈값이면 null)
                .password(request.getPassword())           // 비밀번호
                .name(request.getName())                   // 이름
                .gender(request.getGender())               // 성별 (M / F)
                .nationality(request.getNationality() != null ? request.getNationality() : "KOR") // 국적 (기본값: KOR)
                .emergencyPhone(emergencyPhone)            // 비상 연락처 (빈값이면 null)
                .birthDate(request.getBirthDate())         // 생년월일 (YYYY-MM-DD)
                .build();

        // [3단계: DB의 users 테이블에 새 회원 정보 저장]
        User saved = userRepository.save(user);
        log.info("[회원가입 완료] 전화번호(ID)={}, 이름={}, 생년월일={}", saved.getPhone(), saved.getName(), saved.getBirthDate());

        // [4단계: 프론트엔드에 전달할 응답 객체(UserDto.Response)로 변환하여 반환]
        return convertToResponse(saved);
    }

    /**
     * [로그인 기능]
     * 1. 전화번호(로그인 ID)로 DB에서 회원 정보 조회
     * 2. 비밀번호가 일치하는지 비교 검증
     * 3. 로그인 성공 시 회원 정보를 응답 데이터로 변환 후 반환
     */
    @Transactional(readOnly = true)
    public UserDto.Response login(UserDto.LoginRequest request) {
        String cleanPhone = request.getPhone() != null ? request.getPhone().trim() : "";
        String noHyphenPhone = cleanPhone.replaceAll("-", "");

        User user = userRepository.findByPhone(cleanPhone)
                .or(() -> userRepository.findByLoginId(cleanPhone))
                .or(() -> userRepository.findByPhone(noHyphenPhone))
                .or(() -> userRepository.findByLoginId(noHyphenPhone))
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> (u.getPhone() != null && u.getPhone().replaceAll("-", "").equals(noHyphenPhone)) ||
                                     (u.getLoginId() != null && u.getLoginId().replaceAll("-", "").equals(noHyphenPhone)))
                        .findFirst())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디(휴대폰번호)입니다: " + request.getPhone()));

        if (user.isDeleted()) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }

        String userPw = (user.getPassword() != null) ? user.getPassword() : "";
        if (!userPw.equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("[로그인 성공] ID/전화번호={}, 이름={}", request.getPhone(), user.getName());
        return convertToResponse(user);
    }

    /**
     * [회원 정보 조회 기능]
     * 회원 ID로 DB에서 회원 정보를 조회하여 반환
     * 예매 화면에서 내 정보를 자동으로 불러올 때 사용
     */
    @Transactional(readOnly = true)
    public UserDto.Response getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + id));
        log.info("[회원 정보 조회] 회원ID={}, 이름={}", user.getId(), user.getName());
        return convertToResponse(user);
    }

    /**
     * [전화번호(로그인 ID)로 회원 정보 조회]
     */
    @Transactional(readOnly = true)
    public UserDto.Response getUserByPhone(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전화번호(ID)입니다: " + phone));
        if (user.isDeleted()) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }
        log.info("[회원 정보 조회(전화번호)] 회원ID={}, 전화번호={}, 이름={}", user.getId(), user.getPhone(), user.getName());
        return convertToResponse(user);
    }

    /**
     * [비밀번호 검증 기능]
     * 개인정보 수정 전 본인 확인용으로 사용
     * 1. 회원 ID로 DB에서 회원 조회
     * 2. 입력한 비밀번호와 DB의 비밀번호 비교
     * 3. 불일치 시 예외 발생
     */
    @Transactional(readOnly = true)
    public void verifyPassword(Long id, String password) {
        // [1단계: 회원 조회] 존재하지 않는 회원이면 예외 발생
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + id));

        // [2단계: 비밀번호 검증] 불일치 시 예외 발생
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("[비밀번호 검증 성공] 회원ID={}", id);
    }

    /**
     * [개인정보 수정 기능]
     * 1. 필수값(휴대폰 번호, 이름, 생년월일) 검증
     * 2. 회원 조회
     * 3. 전화번호 변경 시 중복 검사
     * 4. 회원 정보 수정 (비밀번호는 입력된 경우에만 변경)
     * 5. 수정된 결과를 응답 데이터로 변환 후 반환
     */
    @Transactional
    public UserDto.Response updateUser(Long id, UserDto.UpdateRequest request) {
        // [1단계: 필수값 검증]
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("휴대폰 번호는 필수입니다.");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (request.getBirthDate() == null || request.getBirthDate().isBlank()) {
            throw new IllegalArgumentException("생년월일은 필수입니다.");
        }

        // [2단계: 회원 조회]
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + id));

        // [3단계: 전화번호 변경 시 중복 검사] 기존 번호와 다른 경우에만 확인
        if (!user.getPhone().equals(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다: " + request.getPhone());
        }

        // [4단계: 회원 정보 수정]
        user.setPhone(request.getPhone());
        user.setName(request.getName());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setNationality(request.getNationality());
        user.setEmail(request.getEmail());
        user.setEmergencyPhone(request.getEmergencyPhone());

        // 비밀번호는 값이 입력된 경우에만 변경 (비어있으면 기존 비밀번호 유지)
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword());
        }

        log.info("[개인정보 수정 완료] 회원ID={}, 이름={}", user.getId(), user.getName());

        // [5단계: 수정된 정보를 응답 DTO로 변환하여 반환]
        return convertToResponse(user);
    }

    /**
     * [회원 탈퇴 기능]
     * 실제 DB에서 삭제하지 않고 deleted 플래그를 true로 변경하는 소프트 딜리트 방식
     * - 예약 이력 등 연관 데이터를 보존하기 위해 논리적 삭제 처리
     * - 탈퇴 이후 해당 계정으로 로그인 시도 시 예외 발생
     */
    @Transactional
    public void deleteUser(Long id) {
        // [1단계: 회원 조회] 존재하지 않는 회원이면 예외 발생
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + id));

        // [2단계: 이미 탈퇴한 회원인지 확인]
        if (user.isDeleted()) {
            throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
        }

        // [3단계: 탈퇴 처리] deleted 플래그를 true로 변경 (JPA 변경 감지로 자동 UPDATE)
        user.setDeleted(true);

        log.info("[회원 탈퇴 완료] 회원ID={}, 이름={}", user.getId(), user.getName());
    }

    /**
     * [보조 메서드]
     * DB에서 저장/조회된 User 엔티티 데이터를 프론트엔드로 전달할 DTO 객체로 복사/변환
     */
    private UserDto.Response convertToResponse(User user) {
        UserDto.Response res = new UserDto.Response();
        res.setId(user.getId());                         // 고유 회원 번호 (PK)
        res.setPhone(user.getPhone());                   // 전화번호 (로그인 ID)
        res.setEmail(user.getEmail());                   // 이메일
        res.setName(user.getName());                     // 이름
        res.setGender(user.getGender());                 // 성별
        res.setNationality(user.getNationality());       // 국적
        res.setEmergencyPhone(user.getEmergencyPhone()); // 비상연락처
        res.setBirthDate(user.getBirthDate());           // 생년월일
        res.setCreatedAt(user.getCreatedAt());           // 가입 일시
        return res;
    }
}
