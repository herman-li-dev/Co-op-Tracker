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
import com.herman.usercenter.service.JobApplicationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
