package com.herman.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.herman.usercenter.model.domain.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author herman
* @description 针对表【user】的数据库操作Service
* @createDate 2026-05-18 18:06:52
*/
public interface UserService extends IService<User> {
    /**
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @param invitationCode optional invitation code
     * @return new user id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String invitationCode);

    /**
     * User login
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return desensitized user information
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     *
    * @param originUser
    * @return desensitized user information
    */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);
}
