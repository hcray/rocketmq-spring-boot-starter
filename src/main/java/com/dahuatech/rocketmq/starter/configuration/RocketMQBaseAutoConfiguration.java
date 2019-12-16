package com.dahuatech.rocketmq.starter.configuration;

import com.dahuatech.rocketmq.starter.annotation.EnableRocketMQConfiguration;
import com.dahuatech.rocketmq.starter.core.AbstractRocketMQConsumer;
import com.dahuatech.rocketmq.starter.core.AbstractRocketMQProducer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ配置文件
 */
@Configuration
@ConditionalOnBean(annotation = EnableRocketMQConfiguration.class)
@AutoConfigureAfter({AbstractRocketMQProducer.class, AbstractRocketMQConsumer.class})
@EnableConfigurationProperties(RocketMQProperties.class)
public class RocketMQBaseAutoConfiguration implements ApplicationContextAware {

    protected RocketMQProperties mqProperties;

    @Autowired
    public void setMqProperties(RocketMQProperties mqProperties) {
        this.mqProperties = mqProperties;
    }

    protected ConfigurableApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}

