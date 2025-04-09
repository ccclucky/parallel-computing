package team.cclucky.parallel.core.task;

public enum TaskSplitStatus {
    PENDING,      // 等待执行
    RUNNING,      // 正在执行
    COMPLETED,    // 执行完成
    FAILED       // 执行失败
}
