package com.parallel.core;

public enum TaskStatus {
    CREATED,      // 任务创建
    PENDING,      // 等待执行
    RUNNING,      // 正在执行
    COMPLETED,    // 执行完成
    FAILED,       // 执行失败
    CANCELLED;     // 任务取消    

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}