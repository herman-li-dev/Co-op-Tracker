package com.herman.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("application_status_history")
public class JobApplicationStatusHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("application_id")
    private Long applicationId;
    @TableField("user_id")
    private Long userId;
    @TableField("from_status")
    private String fromStatus;
    @TableField("to_status")
    private String toStatus;
    @TableField("changed_at")
    private LocalDateTime changedAt;
}
