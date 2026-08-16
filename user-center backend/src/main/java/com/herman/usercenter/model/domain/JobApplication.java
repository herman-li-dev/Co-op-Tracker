package com.herman.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A co-op or internship application owned by one user.
 */
@Data
@TableName("job_application")
public class JobApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("company_name")
    private String companyName;

    @TableField("job_title")
    private String jobTitle;

    private String location;

    @TableField("job_url")
    private String jobUrl;

    private String status;

    @TableField("work_mode")
    private String workMode;

    @TableField("applied_date")
    private LocalDate appliedDate;

    private LocalDate deadline;

    @TableField("next_follow_up_date")
    private LocalDate nextFollowUpDate;

    @TableField("next_step")
    private String nextStep;

    private String notes;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;
}
