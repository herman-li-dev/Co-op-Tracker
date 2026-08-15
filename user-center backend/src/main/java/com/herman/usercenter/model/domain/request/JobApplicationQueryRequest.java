package com.herman.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class JobApplicationQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private long current = 1;
    private long pageSize = 10;
    private String companyName;
    private String jobTitle;
    private String status;
}
