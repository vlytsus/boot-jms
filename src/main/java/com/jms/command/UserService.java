package com.jms.command;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface UserService {
    String sendUser(String username);
    Stream<UserEntity> queryUser();
}
