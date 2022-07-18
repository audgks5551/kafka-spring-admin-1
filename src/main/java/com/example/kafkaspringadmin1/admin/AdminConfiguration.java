package com.example.kafkaspringadmin1.admin;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

import static com.example.kafkaspringadmin1.CommonConfiguration.BOOTSTRAP_SERVER_HOST;
import static com.example.kafkaspringadmin1.CommonConfiguration.COMPRESSION_TYPE;

@Configuration
public class AdminConfiguration {

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER_HOST);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topic1() {
        return TopicBuilder
                .name("thing1")
                .partitions(10)
                //.replicas(2) // `broker`가 2대 이상일 때 사용 가능, (default 1개)
                .compact() // cleanup.policy => compact or delete (브로커에서 오래된 데이터를 어떻게 처리할 지 정함)
                .build();
    }

    @Bean
    public NewTopic topic2() {
        return TopicBuilder
                .name("thing2")
                .partitions(10) // (default 3개)
                //.replicas(2)
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, COMPRESSION_TYPE) // `producer`에서 데이터를 압축해서 브로커에 보냄
                .build();
    }

    @Bean
    public static NewTopic topic4() {
        return TopicBuilder
                .name("thing4")
                .partitions(3)
                //.assignReplicas(0, Arrays.asList(0, 1)) // 아직 잘 모르겠음
                //.assignReplicas(1, Arrays.asList(1, 2))
                //.assignReplicas(2, Arrays.asList(2, 0))
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, COMPRESSION_TYPE)
                .build();
    }

    /**
     * 여러개의 토픽 생성
     */
    @Bean
    public KafkaAdmin.NewTopics topics567() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name("thing5").replicas(1).build(),
                TopicBuilder.name("thing6").partitions(3).build(),
                TopicBuilder.name("thing7").build()
        );
    }
}
