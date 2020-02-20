package com.jms.command;

import static com.jms.Configurations.MESSAGE_QUEUE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final JmsTemplate jmsTemplate;
  private final UserRepository userRepository;

  @Override
  public String createUser(String username) {
    log.debug("saveUser: ", username);
    jmsTemplate.convertAndSend(MESSAGE_QUEUE,  "JMS received User : " + username );
    UserEntity userEntity = new UserEntity();
    userEntity.setUserId(UUID.randomUUID());
    userEntity.setUsername(username);
    return userRepository.save(userEntity).getUserId().toString();
  }

  @Override
  public Stream<UserEntity> queryUser() {
    return StreamSupport.stream(userRepository.findAll().spliterator(), false);
  }
}
