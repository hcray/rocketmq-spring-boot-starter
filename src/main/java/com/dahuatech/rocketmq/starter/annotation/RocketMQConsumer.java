package com.dahuatech.rocketmq.starter.annotation;

import com.dahuatech.rocketmq.starter.core.MessageExtConstant;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * RocketMQ消费者自动装配注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RocketMQConsumer {
    /**
     * 消费组
     * @return
     */
    String consumerGroup();

    /**
     * 主题
     * @return
     */
    String topic();

    /**
     * 广播模式消费： BROADCASTING
     * 集群模式消费： CLUSTERING
     *
     * @return 消息模式
     */
    String messageMode() default MessageExtConstant.MESSAGE_MODE_CLUSTERING;

    /**
     * 使用线程池并发消费: CONCURRENTLY("CONCURRENTLY"),
     * 单线程消费: ORDERLY("ORDERLY");
     *
     * @return 消费模式
     */
    String consumeMode() default MessageExtConstant.CONSUME_MODE_CONCURRENTLY;

    /**
     * 标签
     * @return
     */
    String[] tag() default {"*"};
}
