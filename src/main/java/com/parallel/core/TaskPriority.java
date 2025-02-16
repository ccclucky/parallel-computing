package com.parallel.core;

public enum TaskPriority {
    LOW(0),
    NORMAL(5),
    HIGH(10),
    URGENT(15);
    
    private final int value;
    
    TaskPriority(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
} 