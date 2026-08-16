package com.herman.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 *  User registration request
 * @author herman
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 5495489516468081204L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;
}
