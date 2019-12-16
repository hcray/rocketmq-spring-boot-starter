package com.dahuatech.rocketmq.starter.exception;

/**
 * RocketMQ的自定义异常
 */
public class RocketMQException extends RuntimeException {
    public RocketMQException(String msg) {
        super(msg);
    }
}
