package com.dahuatech.rocketmq.starter.core;

import com.dahuatech.rocketmq.starter.exception.RocketMQException;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractRocketMQProducer {

    private static final Logger log = LoggerFactory.getLogger(AbstractRocketMQProducer.class);

    private static MessageQueueSelector messageQueueSelector = new SelectMessageQueueByHash();

    public AbstractRocketMQProducer() {
    }

    @Autowired
    private DefaultMQProducer producer;

    /**
     * 同步发送消息
     * @param message  消息体
     * @throws RocketMQException 消息异常
     */
    public void syncSend(Message message) throws RocketMQException {
        try {
            SendResult sendResult = producer.send(message);
            log.debug("send rocketmq message ,messageId : {}", sendResult.getMsgId());
            this.doAfterSyncSend(message, sendResult);
        } catch (Exception e) {
            log.error("send message failure，topic : {}, msgObj {}", message.getTopic(), message);
            throw new RocketMQException("send message failure，topic :" + message.getTopic() + ",e:" + e.getMessage());
        }
    }


    /**
     * 同步发送消息
     * @param message  消息体
     * @param hashKey  用于hash后选择queue的key
     * @throws RocketMQException 消息异常
     */
    public void syncSendOrderly(Message message, String hashKey) throws RocketMQException {
        if(StringUtils.isEmpty(hashKey)) {
            // fall back to normal
            syncSend(message);
        }
        try {
            SendResult sendResult = producer.send(message, messageQueueSelector, hashKey);
            log.debug("send rocketmq message orderly ,messageId : {}", sendResult.getMsgId());
            this.doAfterSyncSend(message, sendResult);
        } catch (Exception e) {
            log.error("send rocketmq message orderly failure，topic : {}, msgObj {}", message.getTopic(), message);
            throw new RocketMQException("send rocketmq message orderly failure，topic :" + message.getTopic() + ",e:" + e.getMessage());
        }
    }

    /**
     * 重写此方法处理发送后的逻辑
     * @param message  发送消息体
     * @param sendResult  发送结果
     */
    public void doAfterSyncSend(Message message, SendResult sendResult) {}

    /**
     * 异步发送消息
     * @param message msgObj
     * @param sendCallback 回调
     * @throws RocketMQException 消息异常
     */
    public void asyncSend(Message message, SendCallback sendCallback) throws RocketMQException {
        try {
            producer.send(message, sendCallback);
            log.debug("send rocketmq message async");
        } catch (Exception e) {
            log.error("send message failure，topic : {}, msgObj {}", message.getTopic(), message);
            throw new RocketMQException("send message failure，topic :" + message.getTopic() + ",e:" + e.getMessage());
        }
    }
}
