package com.herman.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.herman.usercenter.common.ErrorCode;
import com.herman.usercenter.exception.BusinessException;
import com.herman.usercenter.mapper.UserMapper;
import com.herman.usercenter.model.domain.User;
import com.herman.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.herman.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * userserviceimpl class
* @author herman
* @description
* @createDate 2026-05-18 18:06:52
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

        @Resource
        private UserMapper userMapper;

        @Resource
        private PasswordEncoder passwordEncoder;

        /**
         * Optional, temporary setting used only to upgrade existing MD5 hashes on login.
         * Leave unset once every legacy account has been migrated.
         */
        @Value("${security.password.legacy-md5-salt:}")
        private String legacyMd5Salt;

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param invitationCode optional invitation code
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String invitationCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Account and password are required");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Account must be at least 4 characters");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Password must be at least 8 characters");
        }
        if (StringUtils.isNotBlank(invitationCode) && invitationCode.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invitation code must not exceed 32 characters");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (StringUtils.containsWhitespace(userAccount) || matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "This account already exists");
        }
        if (StringUtils.isNotBlank(invitationCode)) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("invitationCode", invitationCode);
            count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "This invitation code is already in use");
            }
        }
        // BCrypt generates a unique salt for every password.
        String encryptPassword = passwordEncoder.encode(userPassword);
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        if (StringUtils.isNotBlank(invitationCode)) {
            user.setInvitationCode(invitationCode);
        }
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // Query by account first so BCrypt can verify the per-user password hash.
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null || !passwordMatches(userPassword, user)) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    private boolean passwordMatches(String rawPassword, User user) {
        String storedPassword = user.getUserPassword();
        if (StringUtils.isBlank(storedPassword)) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        if (StringUtils.isBlank(legacyMd5Salt) || !legacyMd5Matches(rawPassword, storedPassword)) {
            return false;
        }

        user.setUserPassword(passwordEncoder.encode(rawPassword));
        if (!this.updateById(user)) {
            log.warn("Could not upgrade legacy password hash for user {}", user.getId());
            return false;
        }
        log.info("Upgraded legacy password hash for user {}", user.getId());
        return true;
    }

    private boolean legacyMd5Matches(String rawPassword, String storedPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest((legacyMd5Salt + rawPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder encoded = new StringBuilder(32);
            for (byte value : hash) {
                encoded.append(String.format("%02x", value));
            }
            return encoded.toString().equalsIgnoreCase(storedPassword);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("MD5 is unavailable for legacy password migration", exception);
        }
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setInvitationCode(originUser.getInvitationCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

}



