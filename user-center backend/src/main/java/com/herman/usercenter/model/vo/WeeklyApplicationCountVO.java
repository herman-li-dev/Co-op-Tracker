package com.herman.usercenter.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Number of applications submitted during a Monday-based week.
 */
@Data
public class WeeklyApplicationCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate weekStart;

    private Long count;
}
