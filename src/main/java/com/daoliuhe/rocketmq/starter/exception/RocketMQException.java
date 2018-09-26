package com.daoliuhe.rocketmq.starter.exception;

/**
 * Created by yipin on 2017/6/28.
 * RocketMQ的自定义异常
 */
public class RocketMQException extends RuntimeException {
    public RocketMQException(String msg) {
        super(msg);
    }
}
