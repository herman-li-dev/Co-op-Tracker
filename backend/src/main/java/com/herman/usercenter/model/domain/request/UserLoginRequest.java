package com.herman.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * User login request
 *
 * @author herman
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 5495489516468081204L;
    private String userAccount;
    private String userPassword;
}
