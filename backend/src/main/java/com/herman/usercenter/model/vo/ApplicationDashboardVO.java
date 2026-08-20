package com.herman.usercenter.model.vo;

import com.herman.usercenter.model.domain.JobApplication;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Summary data for the signed-in user's application dashboard.
 */
@Data
public class ApplicationDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long total;

    private Long applied;

    private Long interviews;

    private Long offers;

    private Long rejected;

    private BigDecimal interviewRate;

    private BigDecimal offerRate;

    private Long upcomingDeadlines;

    private Long followUpsDue;

    private List<JobApplication> recentApplications;

    private List<ApplicationStatusCountVO> statusDistribution;

    private List<WeeklyApplicationCountVO> weeklyTrend;
}
