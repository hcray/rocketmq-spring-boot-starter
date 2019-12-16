package com.dahuatech.rocketmq.starter.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableRocketMQConfiguration {
}
