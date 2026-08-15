package com.herman.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.herman.usercenter.common.ErrorCode;
import com.herman.usercenter.exception.BusinessException;
import com.herman.usercenter.mapper.JobApplicationMapper;
import com.herman.usercenter.model.domain.JobApplication;
import com.herman.usercenter.model.domain.request.JobApplicationAddRequest;
import com.herman.usercenter.model.domain.request.JobApplicationQueryRequest;
import com.herman.usercenter.model.domain.request.JobApplicationUpdateRequest;
import com.herman.usercenter.model.enums.JobApplicationStatus;
import com.herman.usercenter.model.vo.ApplicationDashboardVO;
import com.herman.usercenter.model.vo.ApplicationStatusCountVO;
import com.herman.usercenter.model.vo.WeeklyApplicationCountVO;
import com.herman.usercenter.service.JobApplicationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobApplicationServiceImpl
        extends ServiceImpl<JobApplicationMapper, JobApplication>
        implements JobApplicationService {

    private static final long MAX_PAGE_SIZE = 50;

    @Override
    public long addApplication(JobApplicationAddRequest addRequest, long userId) {
        if (addRequest == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validateRequiredFields(addRequest.getCompanyName(), addRequest.getJobTitle());

        String status = normalizeStatus(addRequest.getStatus(), JobApplicationStatus.SAVED.name());
        JobApplication application = new JobApplication();
        application.setUserId(userId);
        application.setCompanyName(addRequest.getCompanyName().trim());
        application.setJobTitle(addRequest.getJobTitle().trim());
        application.setLocation(addRequest.getLocation());
        application.setJobUrl(addRequest.getJobUrl());
        application.setStatus(status);
        application.setWorkMode(addRequest.getWorkMode());
        application.setAppliedDate(addRequest.getAppliedDate());
        application.setDeadline(addRequest.getDeadline());
        application.setNextFollowUpDate(addRequest.getNextFollowUpDate());
        application.setNextStep(addRequest.getNextStep());
        application.setNotes(addRequest.getNotes());

        if (!save(application)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Unable to create the application");
        }
        return application.getId();
    }

    @Override
    public boolean updateApplication(JobApplicationUpdateRequest updateRequest, long userId) {
        if (updateRequest == null || updateRequest.getId() == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        JobApplication existing = requireOwnedApplication(updateRequest.getId(), userId);

        if (updateRequest.getCompanyName() != null) {
            validateRequiredFields(updateRequest.getCompanyName(), existing.getJobTitle());
            existing.setCompanyName(updateRequest.getCompanyName().trim());
        }
        if (updateRequest.getJobTitle() != null) {
            validateRequiredFields(existing.getCompanyName(), updateRequest.getJobTitle());
            existing.setJobTitle(updateRequest.getJobTitle().trim());
        }
        if (updateRequest.getStatus() != null) {
            existing.setStatus(normalizeStatus(updateRequest.getStatus(), null));
        }
        if (updateRequest.getLocation() != null) {
            existing.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getJobUrl() != null) {
            existing.setJobUrl(updateRequest.getJobUrl());
        }
        if (updateRequest.getWorkMode() != null) {
            existing.setWorkMode(updateRequest.getWorkMode());
        }
        if (updateRequest.getAppliedDate() != null) {
            existing.setAppliedDate(updateRequest.getAppliedDate());
        }
        if (updateRequest.getDeadline() != null) {
            existing.setDeadline(updateRequest.getDeadline());
        }
        if (updateRequest.getNextFollowUpDate() != null) {
            existing.setNextFollowUpDate(updateRequest.getNextFollowUpDate());
        }
        if (updateRequest.getNextStep() != null) {
            existing.setNextStep(updateRequest.getNextStep());
        }
        if (updateRequest.getNotes() != null) {
            existing.setNotes(updateRequest.getNotes());
        }
        return updateById(existing);
    }

    @Override
    public boolean deleteApplication(long id, long userId) {
        JobApplication application = requireOwnedApplication(id, userId);
        return removeById(application.getId());
    }

    @Override
    public JobApplication getApplication(long id, long userId) {
        return requireOwnedApplication(id, userId);
    }

    @Override
    public Page<JobApplication> listApplications(JobApplicationQueryRequest queryRequest, long userId) {
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        JobApplicationQueryRequest query = queryRequest == null
                ? new JobApplicationQueryRequest()
                : queryRequest;
        long current = Math.max(query.getCurrent(), 1);
        long pageSize = Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE);

        QueryWrapper<JobApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (StringUtils.isNotBlank(query.getCompanyName())) {
            queryWrapper.like("company_name", query.getCompanyName().trim());
        }
        if (StringUtils.isNotBlank(query.getJobTitle())) {
            queryWrapper.like("job_title", query.getJobTitle().trim());
        }
        if (StringUtils.isNotBlank(query.getStatus())) {
            queryWrapper.eq("status", normalizeStatus(query.getStatus(), null));
        }
        queryWrapper.orderByDesc("created_at");
        return page(new Page<>(current, pageSize), queryWrapper);
    }

    @Override
    public ApplicationDashboardVO getDashboard(long userId) {
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        LocalDate today = LocalDate.now();
        ApplicationDashboardVO dashboard = new ApplicationDashboardVO();
        Map<JobApplicationStatus, Long> statusCounts = getStatusCounts(userId);
        dashboard.setTotal(count(ownedApplications(userId)));
        dashboard.setApplied(statusCounts.get(JobApplicationStatus.APPLIED));
        dashboard.setInterviews(statusCounts.get(JobApplicationStatus.INTERVIEW));
        dashboard.setOffers(statusCounts.get(JobApplicationStatus.OFFER));
        dashboard.setRejected(statusCounts.get(JobApplicationStatus.REJECTED));
        dashboard.setUpcomingDeadlines(count(ownedApplications(userId)
                .ge("deadline", today)
                .le("deadline", today.plusDays(7))));
        dashboard.setFollowUpsDue(count(ownedApplications(userId)
                .le("next_follow_up_date", today)));

        List<JobApplication> recentApplications = list(ownedApplications(userId)
                .orderByDesc("created_at")
                .last("LIMIT 5"));
        dashboard.setRecentApplications(recentApplications);
        dashboard.setStatusDistribution(toStatusDistribution(statusCounts));
        dashboard.setWeeklyTrend(getWeeklyTrend(userId, today));
        return dashboard;
    }

    private Map<JobApplicationStatus, Long> getStatusCounts(long userId) {
        Map<JobApplicationStatus, Long> statusCounts = new EnumMap<>(JobApplicationStatus.class);
        for (JobApplicationStatus status : JobApplicationStatus.values()) {
            statusCounts.put(status, countByStatus(userId, status));
        }
        return statusCounts;
    }

    private List<ApplicationStatusCountVO> toStatusDistribution(
            Map<JobApplicationStatus, Long> statusCounts) {
        List<ApplicationStatusCountVO> distribution = new ArrayList<>();
        for (JobApplicationStatus status : JobApplicationStatus.values()) {
            ApplicationStatusCountVO item = new ApplicationStatusCountVO();
            item.setStatus(status.name());
            item.setCount(statusCounts.get(status));
            distribution.add(item);
        }
        return distribution;
    }

    private List<WeeklyApplicationCountVO> getWeeklyTrend(long userId, LocalDate today) {
        LocalDate currentWeekStart = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate firstWeekStart = currentWeekStart.minusWeeks(7);

        Map<LocalDate, Long> weeklyCounts = new LinkedHashMap<>();
        for (int week = 0; week < 8; week++) {
            weeklyCounts.put(firstWeekStart.plusWeeks(week), 0L);
        }

        List<JobApplication> applications = list(ownedApplications(userId)
                .ge("applied_date", firstWeekStart)
                .le("applied_date", today));
        for (JobApplication application : applications) {
            if (application == null || application.getAppliedDate() == null) {
                continue;
            }
            LocalDate appliedDate = application.getAppliedDate();
            LocalDate weekStart = appliedDate.with(
                    TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            if (weeklyCounts.containsKey(weekStart)) {
                weeklyCounts.put(weekStart, weeklyCounts.get(weekStart) + 1);
            }
        }

        List<WeeklyApplicationCountVO> trend = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : weeklyCounts.entrySet()) {
            WeeklyApplicationCountVO item = new WeeklyApplicationCountVO();
            item.setWeekStart(entry.getKey());
            item.setCount(entry.getValue());
            trend.add(item);
        }
        return trend;
    }

    private long countByStatus(long userId, JobApplicationStatus status) {
        return count(ownedApplications(userId).eq("status", status.name()));
    }

    private QueryWrapper<JobApplication> ownedApplications(long userId) {
        return new QueryWrapper<JobApplication>().eq("user_id", userId);
    }

    private JobApplication requireOwnedApplication(long id, long userId) {
        if (id <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<JobApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("user_id", userId);
        JobApplication application = getOne(queryWrapper);
        if (application == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "Application not found");
        }
        return application;
    }

    private void validateRequiredFields(String companyName, String jobTitle) {
        if (StringUtils.isBlank(companyName) || StringUtils.isBlank(jobTitle)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Company and job title are required");
        }
        if (companyName.length() > 128 || jobTitle.length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Company or job title is too long");
        }
    }

    private String normalizeStatus(String status, String defaultStatus) {
        String value = StringUtils.isBlank(status) ? defaultStatus : status.trim().toUpperCase();
        if (!JobApplicationStatus.isValid(value)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Unsupported application status");
        }
        return value;
    }
}
