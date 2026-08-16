package com.herman.usercenter.model.domain.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class JobApplicationQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private long current = 1;
    private long pageSize = 10;
    private String companyName;
    private String jobTitle;
    private String status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appliedDateStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appliedDateEnd;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate deadlineStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate deadlineEnd;
    private String sortField;
    private String sortOrder;
}
