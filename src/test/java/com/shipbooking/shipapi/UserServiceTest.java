package com.shipbooking.shipapi;

import com.shipbooking.shipapi.dto.UserDto;
import com.shipbooking.shipapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testLoginWithSeededUser() {
        UserDto.LoginRequest request = new UserDto.LoginRequest();
        request.setPhone("01012341234");
        request.setPassword("1234");

        UserDto.Response response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getPhone()).isEqualTo("01012341234");
        assertThat(response.getName()).isEqualTo("정현영");
    }
}
