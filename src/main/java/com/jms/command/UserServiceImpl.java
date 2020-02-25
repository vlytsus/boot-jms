package com.jms.command;

import static com.jms.Configurations.MESSAGE_QUEUE;

import brave.Tracer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final JmsTemplate jmsTemplate;
  private final UserRepository userRepository;
  private final Tracer tracer;

  @NewSpan
  @SneakyThrows
  @Override
  public String createUser(String username) {
    log.debug("saveUser: ", username);
    jmsTemplate.convertAndSend(MESSAGE_QUEUE,  "JMS received User : " + username );
    tracer.currentSpan().annotate("create entity");
    UserEntity userEntity = new UserEntity();
    tracer.currentSpan().annotate("setUsername");
    userEntity.setUsername(username);
    tracer.newTrace().annotate("Thread.sleep(333)");
    Thread.sleep(333);
    tracer.currentSpan().annotate("save entity");
    return userRepository.save(userEntity).getUserId().toString();
  }

  @Override
  @NewSpan
  public Stream<UserEntity> queryUser() {
    return StreamSupport.stream(userRepository.findAll().spliterator(), false);
  }
}
