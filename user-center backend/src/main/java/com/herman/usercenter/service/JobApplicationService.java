package com.herman.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.herman.usercenter.model.domain.JobApplication;
import com.herman.usercenter.model.domain.request.JobApplicationAddRequest;
import com.herman.usercenter.model.domain.request.JobApplicationQueryRequest;
import com.herman.usercenter.model.domain.request.JobApplicationUpdateRequest;
import com.herman.usercenter.model.vo.ApplicationDashboardVO;

public interface JobApplicationService extends IService<JobApplication> {

    long addApplication(JobApplicationAddRequest addRequest, long userId);

    boolean updateApplication(JobApplicationUpdateRequest updateRequest, long userId);

    boolean deleteApplication(long id, long userId);

    JobApplication getApplication(long id, long userId);

    Page<JobApplication> listApplications(JobApplicationQueryRequest queryRequest, long userId);

    ApplicationDashboardVO getDashboard(long userId);
}
