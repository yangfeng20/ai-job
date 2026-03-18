package com.maple.ai.job.hunting.frame.exp;

import lombok.Getter;

/**
 * ai能力 异常
 *
 * @author maple
 * @since 2025/02/12
 */
@Getter
public class AIPowerException extends RuntimeException {

    private final String name;

    private final String key;


    public AIPowerException(String name, String message) {
        super(message);
        this.name = name;
        this.key = "";
    }

    public AIPowerException(String name, String key, String message) {
        super(message);
        this.name = name;
        this.key = key;
    }

    public String getKey() {
        return name + "-" + key;
    }

}
