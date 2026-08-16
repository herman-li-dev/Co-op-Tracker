package com.herman.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.herman.usercenter.common.ErrorCode;
import com.herman.usercenter.exception.BusinessException;
import com.herman.usercenter.mapper.JobApplicationMapper;
import com.herman.usercenter.mapper.JobApplicationStatusHistoryMapper;
import com.herman.usercenter.model.domain.JobApplication;
import com.herman.usercenter.model.domain.JobApplicationStatusHistory;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final int MAX_NOTES_LENGTH = 2000;

    @Resource
    private JobApplicationStatusHistoryMapper statusHistoryMapper;

    @Override
    @Transactional
    public long addApplication(JobApplicationAddRequest addRequest, long userId) {
        if (addRequest == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
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
        validateApplication(application);

        if (!save(application)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Unable to create the application");
        }
        recordStatusChange(application, null, application.getStatus());
        return application.getId();
    }

    @Override
    @Transactional
    public boolean updateApplication(JobApplicationUpdateRequest updateRequest, long userId) {
        if (updateRequest == null || updateRequest.getId() == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        JobApplication existing = requireOwnedApplication(updateRequest.getId(), userId);
        String previousStatus = existing.getStatus();

        if (updateRequest.getCompanyName() != null) {
            existing.setCompanyName(updateRequest.getCompanyName().trim());
        }
        if (updateRequest.getJobTitle() != null) {
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
        validateApplication(existing);
        boolean updated = updateById(existing);
        if (updated && !StringUtils.equals(previousStatus, existing.getStatus())) {
            recordStatusChange(existing, previousStatus, existing.getStatus());
        }
        return updated;
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
        validateDateRange(query.getAppliedDateStart(), query.getAppliedDateEnd(), "applied date");
        validateDateRange(query.getDeadlineStart(), query.getDeadlineEnd(), "deadline");
        queryWrapper.ge(query.getAppliedDateStart() != null,
                "applied_date", query.getAppliedDateStart());
        queryWrapper.le(query.getAppliedDateEnd() != null,
                "applied_date", query.getAppliedDateEnd());
        queryWrapper.ge(query.getDeadlineStart() != null,
                "deadline", query.getDeadlineStart());
        queryWrapper.le(query.getDeadlineEnd() != null,
                "deadline", query.getDeadlineEnd());

        String sortColumn = getSortColumn(query.getSortField());
        if (sortColumn == null) {
            queryWrapper.orderByDesc("created_at");
        } else {
            boolean ascending = "asc".equalsIgnoreCase(query.getSortOrder())
                    || "ascend".equalsIgnoreCase(query.getSortOrder());
            queryWrapper.orderBy(true, ascending, sortColumn);
        }
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
        dashboard.setInterviewRate(calculateRate(userId, JobApplicationStatus.INTERVIEW, dashboard.getTotal()));
        dashboard.setOfferRate(calculateRate(userId, JobApplicationStatus.OFFER, dashboard.getTotal()));
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

    @Override
    public List<JobApplicationStatusHistory> getStatusHistory(long applicationId, long userId) {
        requireOwnedApplication(applicationId, userId);
        return statusHistoryMapper.selectList(new QueryWrapper<JobApplicationStatusHistory>()
                .eq("application_id", applicationId)
                .eq("user_id", userId)
                .orderByAsc("changed_at"));
    }

    private void recordStatusChange(JobApplication application, String fromStatus, String toStatus) {
        JobApplicationStatusHistory history = new JobApplicationStatusHistory();
        history.setApplicationId(application.getId());
        history.setUserId(application.getUserId());
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        statusHistoryMapper.insert(history);
    }

    private BigDecimal calculateRate(long userId, JobApplicationStatus status, Long total) {
        if (total == null || total == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        long converted = statusHistoryMapper.selectList(new QueryWrapper<JobApplicationStatusHistory>()
                .select("DISTINCT application_id")
                .eq("user_id", userId)
                .eq("to_status", status.name())).size();
        return BigDecimal.valueOf(converted * 100.0 / total)
                .setScale(1, RoundingMode.HALF_UP);
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

    private void validateApplication(JobApplication application) {
        if (StringUtils.isBlank(application.getCompanyName())
                || StringUtils.isBlank(application.getJobTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Company and job title are required");
        }
        if (application.getCompanyName().length() > 128
                || application.getJobTitle().length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Company or job title is too long");
        }
        if (application.getLocation() != null && application.getLocation().length() > 128) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Location is too long");
        }
        if (application.getJobUrl() != null && application.getJobUrl().length() > 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Job URL is too long");
        }
        validateJobUrl(application.getJobUrl());
        if (application.getNextStep() != null && application.getNextStep().length() > 255) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Next step is too long");
        }
        if (application.getNotes() != null
                && application.getNotes().length() > MAX_NOTES_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Notes are too long");
        }
        if (application.getAppliedDate() != null && application.getDeadline() != null
                && application.getDeadline().isBefore(application.getAppliedDate())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "Deadline cannot be earlier than the applied date");
        }
        if (StringUtils.isNotBlank(application.getWorkMode())
                && !isValidWorkMode(application.getWorkMode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Unsupported work mode");
        }
    }

    private void validateJobUrl(String jobUrl) {
        if (StringUtils.isBlank(jobUrl)) {
            return;
        }
        try {
            URI uri = new URI(jobUrl);
            boolean validScheme = "http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme());
            if (!validScheme || StringUtils.isBlank(uri.getHost())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Enter a valid HTTP or HTTPS URL");
            }
        } catch (URISyntaxException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Enter a valid HTTP or HTTPS URL");
        }
    }

    private boolean isValidWorkMode(String workMode) {
        return "ON_SITE".equalsIgnoreCase(workMode)
                || "HYBRID".equalsIgnoreCase(workMode)
                || "REMOTE".equalsIgnoreCase(workMode);
    }

    private void validateDateRange(LocalDate start, LocalDate end, String fieldName) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "The " + fieldName + " start cannot be after its end");
        }
    }

    private String getSortColumn(String sortField) {
        if ("companyName".equals(sortField)) {
            return "company_name";
        }
        if ("jobTitle".equals(sortField)) {
            return "job_title";
        }
        if ("status".equals(sortField)) {
            return "status";
        }
        if ("appliedDate".equals(sortField)) {
            return "applied_date";
        }
        if ("deadline".equals(sortField)) {
            return "deadline";
        }
        if ("createdAt".equals(sortField)) {
            return "created_at";
        }
        return null;
    }

    private String normalizeStatus(String status, String defaultStatus) {
        String value = StringUtils.isBlank(status) ? defaultStatus : status.trim().toUpperCase();
        if (!JobApplicationStatus.isValid(value)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Unsupported application status");
        }
        return value;
    }
}
