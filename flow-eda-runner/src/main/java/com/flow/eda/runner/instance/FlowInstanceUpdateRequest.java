package com.flow.eda.runner.instance;

import java.time.LocalDateTime;

/**
 * 实例更新请求
 */
public class FlowInstanceUpdateRequest {
    private String status;
    private LocalDateTime endTime;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
