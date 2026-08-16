package com.herman.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.herman.usercenter.common.BaseResponse;
import com.herman.usercenter.common.ErrorCode;
import com.herman.usercenter.common.ResultUtils;
import com.herman.usercenter.exception.BusinessException;
import com.herman.usercenter.model.domain.JobApplication;
import com.herman.usercenter.model.domain.JobApplicationStatusHistory;
import com.herman.usercenter.model.domain.User;
import com.herman.usercenter.model.domain.request.JobApplicationAddRequest;
import com.herman.usercenter.model.domain.request.JobApplicationDeleteRequest;
import com.herman.usercenter.model.domain.request.JobApplicationQueryRequest;
import com.herman.usercenter.model.domain.request.JobApplicationUpdateRequest;
import com.herman.usercenter.model.vo.ApplicationDashboardVO;
import com.herman.usercenter.service.JobApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.herman.usercenter.contant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/application")
public class JobApplicationController {

    @Resource
    private JobApplicationService jobApplicationService;

    @PostMapping("/add")
    public BaseResponse<Long> addApplication(
            @RequestBody JobApplicationAddRequest addRequest,
            HttpServletRequest request) {
        long id = jobApplicationService.addApplication(addRequest, getLoginUserId(request));
        return ResultUtils.success(id);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateApplication(
            @RequestBody JobApplicationUpdateRequest updateRequest,
            HttpServletRequest request) {
        boolean result = jobApplicationService.updateApplication(updateRequest, getLoginUserId(request));
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApplication(
            @RequestBody JobApplicationDeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = jobApplicationService.deleteApplication(
                deleteRequest.getId(), getLoginUserId(request));
        return ResultUtils.success(result);
    }

    @GetMapping("/get")
    public BaseResponse<JobApplication> getApplication(
            @RequestParam long id,
            HttpServletRequest request) {
        JobApplication application = jobApplicationService.getApplication(id, getLoginUserId(request));
        return ResultUtils.success(application);
    }

    @GetMapping("/list")
    public BaseResponse<Page<JobApplication>> listApplications(
            JobApplicationQueryRequest queryRequest,
            HttpServletRequest request) {
        Page<JobApplication> page = jobApplicationService.listApplications(
                queryRequest, getLoginUserId(request));
        return ResultUtils.success(page);
    }

    @GetMapping("/dashboard")
    public BaseResponse<ApplicationDashboardVO> getDashboard(HttpServletRequest request) {
        ApplicationDashboardVO dashboard = jobApplicationService.getDashboard(getLoginUserId(request));
        return ResultUtils.success(dashboard);
    }

    @GetMapping("/history")
    public BaseResponse<List<JobApplicationStatusHistory>> getStatusHistory(
            @RequestParam long id,
            HttpServletRequest request) {
        return ResultUtils.success(
                jobApplicationService.getStatusHistory(id, getLoginUserId(request)));
    }

    private long getLoginUserId(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!(userObject instanceof User)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User loginUser = (User) userObject;
        if (loginUser.getId() == null || loginUser.getId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return loginUser.getId();
    }
}
