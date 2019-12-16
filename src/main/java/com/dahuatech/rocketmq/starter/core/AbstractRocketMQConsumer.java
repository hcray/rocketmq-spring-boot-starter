package com.dahuatech.rocketmq.starter.core;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRocketMQConsumer<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractRocketMQProducer.class);

    protected static Gson gson = new Gson();

    /**
     * 反序列化解析消息
     *
     * @param message  消息体
     * @return 序列化结果
     */
    protected T parseMessage(MessageExt message) {
        if (message == null || message.getBody() == null) {
            return null;
        }
        final Type type = this.getMessageType();
        if (type instanceof Class) {
            try {
                T data = gson.fromJson(new String(message.getBody()), type);
                return data;
            } catch (JsonSyntaxException e) {
                log.error("parse message json fail : {}", e.getMessage());
            }
        } else {
            log.warn("Parse msg error. {}", message);
        }
        return null;
    }

    protected Map<String, Object> parseExtParam(MessageExt message) {
        Map<String, Object> extMap = new HashMap<>();

        // parse message property
        extMap.put(MessageExtConstant.PROPERTY_TOPIC, message.getTopic());
        extMap.putAll(message.getProperties());

        // parse messageExt property
        extMap.put(MessageExtConstant.PROPERTY_EXT_BORN_HOST, message.getBornHost());
        extMap.put(MessageExtConstant.PROPERTY_EXT_BORN_TIMESTAMP, message.getBornTimestamp());
        extMap.put(MessageExtConstant.PROPERTY_EXT_COMMIT_LOG_OFFSET, message.getCommitLogOffset());
        extMap.put(MessageExtConstant.PROPERTY_EXT_MSG_ID, message.getMsgId());
        extMap.put(MessageExtConstant.PROPERTY_EXT_PREPARED_TRANSACTION_OFFSET, message.getPreparedTransactionOffset());
        extMap.put(MessageExtConstant.PROPERTY_EXT_QUEUE_ID, message.getQueueId());
        extMap.put(MessageExtConstant.PROPERTY_EXT_QUEUE_OFFSET, message.getQueueOffset());
        extMap.put(MessageExtConstant.PROPERTY_EXT_RECONSUME_TIMES, message.getReconsumeTimes());
        extMap.put(MessageExtConstant.PROPERTY_EXT_STORE_HOST, message.getStoreHost());
        extMap.put(MessageExtConstant.PROPERTY_EXT_STORE_SIZE, message.getStoreSize());
        extMap.put(MessageExtConstant.PROPERTY_EXT_STORE_TIMESTAMP, message.getStoreTimestamp());
        extMap.put(MessageExtConstant.PROPERTY_EXT_SYS_FLAG, message.getSysFlag());
        extMap.put(MessageExtConstant.PROPERTY_EXT_BODY_CRC, message.getBodyCRC());

        return extMap;
    }

    /**
     * 解析消息类型
     *
     * @return 消息类型
     */
    protected Type getMessageType() {
        Type superType = this.getClass().getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Assert.isTrue(actualTypeArguments.length == 1, "Number of type arguments must be 1");
            return actualTypeArguments[0];
        } else {
            // 如果没有定义泛型，解析为Object
            return Object.class;
//            throw new RuntimeException("Unkown parameterized type.");
        }
    }
}
