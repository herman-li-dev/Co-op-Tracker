package com.herman.usercenter.service;

import com.herman.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.herman.usercenter.exception.BusinessException;

import javax.annotation.Resource;

/**
 *
 * userService test
 *
 * Integration tests for user registration.
 */

@SpringBootTest
@Transactional
class UserServiceTest {

    @Resource
    private UserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Test
    void testAddUser(){
        //new User().var
        User user = new User();
        user.setId(0L);
        user.setUserName("Test User");
        user.setUserAccount("123");
        user.setAvatarUrl("");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("456");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {
        // 空账号或密码：当前业务逻辑会抛异常
        Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister("testUser", "", "12345678", ""));

        // 账号少于 4 位：抛异常
        Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister("yu", "12345678", "12345678", ""));

        // 密码少于 8 位：抛异常
        Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister("testUser", "123456", "123456", ""));

        // 账号含特殊字符：返回 -1
        Assertions.assertEquals(-1,
                userService.userRegister("yu pi", "12345678", "12345678", ""));

        // 两次密码不一致：返回 -1
        Assertions.assertEquals(-1,
                userService.userRegister("testUser", "12345678", "123456789", ""));

        // 正常注册：返回新用户 ID
        String account = "test" + System.nanoTime();
        long result = userService.userRegister(account, "12345678", "12345678", "");
        Assertions.assertTrue(result > 0);
        User registeredUser = userService.getById(result);
        Assertions.assertNotNull(registeredUser);
        Assertions.assertTrue(registeredUser.getUserPassword().startsWith("$2"));
        Assertions.assertTrue(passwordEncoder.matches("12345678", registeredUser.getUserPassword()));
        Assertions.assertFalse(passwordEncoder.matches("incorrect-password", registeredUser.getUserPassword()));

        // 重复账号：抛异常
        Assertions.assertThrows(BusinessException.class,
                () -> userService.userRegister(account, "12345678", "12345678", ""));
    }
}
