package com.daoliuhe.rocketmq.starter.configuration;

import com.daoliuhe.rocketmq.starter.annotation.RocketMQConsumer;
import com.daoliuhe.rocketmq.starter.core.AbstractRocketMQPushConsumer;
import com.daoliuhe.rocketmq.starter.core.MessageExtConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 自动装配消息消费者
 */
@Configuration
@ConditionalOnBean(RocketMQBaseAutoConfiguration.class)
public class RocketMQConsumerAutoConfiguration extends RocketMQBaseAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RocketMQConsumerAutoConfiguration.class);

    // 维护一份map用于检测是否用同样的consumerGroup订阅了不同的topic+tag
    private Map<String, String> validConsumerMap;

    @PostConstruct
    public void init() throws Exception {
        validConsumerMap = new HashMap<>();
        //消费者
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RocketMQConsumer.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            publishConsumer(entry.getKey(), entry.getValue());
        }
        // 清空map，等待回收
        validConsumerMap = null;
    }

    private void publishConsumer(String beanName, Object bean) throws Exception {
        RocketMQConsumer mqConsumer = applicationContext.findAnnotationOnBean(beanName, RocketMQConsumer.class);
        if (StringUtils.isEmpty(mqProperties.getNameServerAddress())) {
            throw new RuntimeException("name server address must be defined");
        }
        Assert.notNull(mqConsumer.consumerGroup(), "consumer's consumerGroup must be defined");
        Assert.notNull(mqConsumer.topic(), "consumer's topic must be defined");
        if (!AbstractRocketMQPushConsumer.class.isAssignableFrom(bean.getClass())) {
            throw new RuntimeException(bean.getClass().getName() + " - consumer未实现Consumer抽象类");
        }
        Environment environment = applicationContext.getEnvironment();

        String consumerGroup = environment.resolvePlaceholders(mqConsumer.consumerGroup());
        String topic = environment.resolvePlaceholders(mqConsumer.topic());
        String tags = "*";
        if (mqConsumer.tag().length == 1) {
            tags = environment.resolvePlaceholders(mqConsumer.tag()[0]);
        } else if (mqConsumer.tag().length > 1) {
            tags = StringUtils.join(mqConsumer.tag(), "||");
        }

        // 检查consumerGroup
        if (!StringUtils.isEmpty(validConsumerMap.get(consumerGroup))) {
            String exist = validConsumerMap.get(consumerGroup);
            throw new RuntimeException("消费组重复订阅，请新增消费组用于新的topic和tag组合: " + consumerGroup + "已经订阅了" + exist);
        } else {
            validConsumerMap.put(consumerGroup, topic + "-" + tags);
        }

        // 配置push consumer
        if (AbstractRocketMQPushConsumer.class.isAssignableFrom(bean.getClass())) {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
            consumer.setNamesrvAddr(mqProperties.getNameServerAddress());
            consumer.setMessageModel(MessageModel.valueOf(mqConsumer.messageMode()));
            consumer.subscribe(topic, tags);
            consumer.setInstanceName(UUID.randomUUID().toString());
            consumer.setVipChannelEnabled(mqProperties.getVipChannelEnabled());
            AbstractRocketMQPushConsumer abstractMQPushConsumer = (AbstractRocketMQPushConsumer) bean;
            if (MessageExtConstant.CONSUME_MODE_CONCURRENTLY.equals(mqConsumer.consumeMode())) {
                consumer.registerMessageListener((List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) ->
                        abstractMQPushConsumer.dealMessage(list, consumeConcurrentlyContext));
            } else if (MessageExtConstant.CONSUME_MODE_ORDERLY.equals(mqConsumer.consumeMode())) {
                consumer.registerMessageListener((List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) ->
                        abstractMQPushConsumer.dealMessage(list, consumeOrderlyContext));
            } else {
                throw new RuntimeException("unknown consume mode ! only support CONCURRENTLY and ORDERLY");
            }
            abstractMQPushConsumer.setConsumer(consumer);

            consumer.start();
        }

        log.info(String.format("%s is ready to subscribe message", bean.getClass().getName()));
    }

}