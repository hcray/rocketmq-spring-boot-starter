package com.dahuatech.rocketmq.starter.core;

import com.dahuatech.rocketmq.starter.enums.DelayTimeLevel;
import com.dahuatech.rocketmq.starter.annotation.RocketMQKey;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

public class MessageBuilder {

    private static final Logger log = LoggerFactory.getLogger(MessageBuilder.class);

    private static Gson gson = new Gson();

    private static final String[] DELAY_ARRAY = "1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h".split(" ");

    private String topic;
    private String tag;
    private String key;
    private Object message;
    private Integer delayTimeLevel;

    public static MessageBuilder of(String topic, String tag) {
        MessageBuilder builder = new MessageBuilder();
        builder.setTopic(topic);
        builder.setTag(tag);
        return builder;
    }

    public static MessageBuilder of(Object message) {
        MessageBuilder builder = new MessageBuilder();
        builder.setMessage(message);
        return builder;
    }

    public MessageBuilder topic(String topic) {
        this.topic = topic;
        return this;
    }

    public MessageBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }

    public MessageBuilder key(String key) {
        this.key = key;
        return this;
    }

    public MessageBuilder delayTimeLevel(DelayTimeLevel delayTimeLevel) {
        this.delayTimeLevel = delayTimeLevel.getLevel();
        return this;
    }

    public Message build() {
        String messageKey = "";
        try {
            Field[] fields = message.getClass().getDeclaredFields();
            for (Field field : fields) {
                Annotation[] allFAnnos = field.getAnnotations();
                if (allFAnnos.length > 0) {
                    for (int i = 0; i < allFAnnos.length; i++) {
                        if (allFAnnos[i].annotationType().equals(RocketMQKey.class)) {
                            field.setAccessible(true);
                            RocketMQKey mqKey = RocketMQKey.class.cast(allFAnnos[i]);
                            messageKey = StringUtils.isEmpty(mqKey.prefix()) ? field.get(message).toString() : (mqKey.prefix() + field.get(message).toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("parse key error : {}", e.getMessage());
        }
        String str = gson.toJson(message);
        if (StringUtils.isEmpty(topic)) {
            if (StringUtils.isEmpty(getTopic())) {
                throw new RuntimeException("no topic defined to send this message");
            }
        }
        Message message = new Message(topic, str.getBytes(Charset.forName("utf-8")));
        if (!StringUtils.isEmpty(tag)) {
            message.setTags(tag);
        }
        if (StringUtils.isNotEmpty(messageKey)) {
            message.setKeys(messageKey);
        }
        if (delayTimeLevel != null && delayTimeLevel > 0 && delayTimeLevel <= DELAY_ARRAY.length) {
            message.setDelayTimeLevel(delayTimeLevel);
        }
        return message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Integer getDelayTimeLevel() {
        return delayTimeLevel;
    }

    public void setDelayTimeLevel(Integer delayTimeLevel) {
        this.delayTimeLevel = delayTimeLevel;
    }
}
