package com.jms.command;

import static com.jms.Configurations.MESSAGE_QUEUE;

import javax.jms.Session;
import javax.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserConsumer {

  @SneakyThrows
  @Transactional
  @NewSpan("receiveMessage span name")
  @JmsListener(destination = MESSAGE_QUEUE)
  public void receiveMessage(@SpanTag("payload") @Payload String payload,
      @Headers MessageHeaders headers,
      Message message, Session session) {

    Thread.sleep(1000);

    log.info("##################################");
    log.info("received payload: <" + payload + ">");
    log.info("##################################");
  }
}
