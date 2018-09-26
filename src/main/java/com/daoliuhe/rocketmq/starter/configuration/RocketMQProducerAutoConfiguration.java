package com.daoliuhe.rocketmq.starter.configuration;

import com.daoliuhe.rocketmq.starter.annotation.RocketMQProducer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 自动装配消息生产者
 */
@Configuration
@ConditionalOnBean(RocketMQBaseAutoConfiguration.class)
public class RocketMQProducerAutoConfiguration extends RocketMQBaseAutoConfiguration {

    private static DefaultMQProducer producer;

    @Bean
    public DefaultMQProducer exposeProducer() throws Exception {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RocketMQProducer.class);
        //对于仅仅只存在消息消费者的项目，无需构建生产者
        if (CollectionUtils.isEmpty(beans)) {
            return null;
        }
        if (producer == null) {
            Assert.notNull(mqProperties.getProducerGroup(), "producer group must be defined");
            Assert.notNull(mqProperties.getNameServerAddress(), "name server address must be defined");
            producer = new DefaultMQProducer(mqProperties.getProducerGroup());
            producer.setNamesrvAddr(mqProperties.getNameServerAddress());
            producer.setSendMsgTimeout(mqProperties.getSendMsgTimeout());
            producer.setSendMessageWithVIPChannel(mqProperties.getVipChannelEnabled());
            producer.start();
        }
        return producer;
    }

    public static DefaultMQProducer getProducer() {
        return producer;
    }

    public static void setProducer(DefaultMQProducer producer) {
        RocketMQProducerAutoConfiguration.producer = producer;
    }
}
