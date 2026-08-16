package com.herman.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class JobApplicationUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String companyName;
    private String jobTitle;
    private String location;
    private String jobUrl;
    private String status;
    private String workMode;
    private LocalDate appliedDate;
    private LocalDate deadline;
    private LocalDate nextFollowUpDate;
    private String nextStep;
    private String notes;
}
