package com.herman.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class JobApplicationDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
}
