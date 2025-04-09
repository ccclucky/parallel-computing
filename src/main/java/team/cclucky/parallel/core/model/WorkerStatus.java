package team.cclucky.parallel.core.model;

import java.io.Serializable;

public enum WorkerStatus implements Serializable {
    IDLE,       // 空闲状态
    RUNNING,    // 正在执行任务
    STOPPED,    // 已停止
    ERROR       // 错误状态
} 