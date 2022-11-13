package com.plumroc.test;


import com.plumroc.config.RabbitMQConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ProducerTest {

    //1.注入RabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSend() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "ems.queues", "boot mq1 hello~~~");
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "ems.queues", "boot mq2 hello~~~");
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "ems.queues", "boot mq3 hello~~~");
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "ems.queues", "boot mq4 hello~~~");
    }
}
