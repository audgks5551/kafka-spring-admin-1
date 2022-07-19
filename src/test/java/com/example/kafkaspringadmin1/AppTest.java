package com.example.kafkaspringadmin1;

import org.apache.kafka.clients.admin.TopicDescription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9095",
                "auto.create.topics.enable=false"
        },
        ports = { 9095 }
)
class AppTest {
    private Logger log = LoggerFactory.getLogger(AppTest.class);

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Autowired
    private KafkaTemplate<Integer, String> template;

    @Test
    public void 토픽을_등록하지_않은_토픽정보_불러오기() {

        // given
        String notExistTopic = "notExistTopic";

        // when, then
        Assertions.assertThrows(KafkaException.class, () -> {
            kafkaAdmin.describeTopics(notExistTopic);
        });
    }

    @Test
    public void 특정_토픽의_정보를_불러오기() {
        // given
        String topicName = "thing1";

        // when
        TopicDescription topicDescription = kafkaAdmin.describeTopics(topicName).get(topicName);
        String name = topicDescription.name();
        int size = topicDescription.partitions().size();

        // then
        assertThat(name).isEqualTo(topicName);
        assertThat(size).isEqualTo(10);
    }

    /**
     * TODO
     *  1. autoflush와 flush사이에서 무엇이 좋은지 확인하기
     */
    @Test
    public void 토픽에_데이터_하나_비동기전송() {
        // given
        String topicName = "thing1";
        Integer key = 1;
        String value = "something";
        Map<String, Object> resultMap = new HashMap<>();

        // when
        ListenableFuture<SendResult<Integer, String>> future = template.send(topicName, key, value);
        template.flush();

        future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                resultMap.put("topic", result.getProducerRecord().topic());
                resultMap.put("key", result.getProducerRecord().key());
                resultMap.put("value", result.getProducerRecord().value());
            }

            /**
             * 오류나는 경우
             *  1. key의 type과 value의 type이 다를 때
             */
            @Override
            public void onFailure(Throwable ex) {
                log.warn("{}", ex.getCause());
            }
        });

        // then
        assertThat(resultMap.get("topic")).isEqualTo(topicName);
        assertThat(resultMap.get("key")).isEqualTo(key);
        assertThat(resultMap.get("value")).isEqualTo(value);
    }
}
