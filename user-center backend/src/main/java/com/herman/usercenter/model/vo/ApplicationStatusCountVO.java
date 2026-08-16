package com.herman.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Number of applications currently in one pipeline status.
 */
@Data
public class ApplicationStatusCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;

    private Long count;
}
