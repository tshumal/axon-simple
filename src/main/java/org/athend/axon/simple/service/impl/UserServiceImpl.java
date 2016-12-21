
package org.athend.axon.simple.service.impl;

import org.athend.axon.simple.commands.UserCreateCommand;
import org.athend.axon.simple.commands.UserLockCommand;
import org.athend.axon.simple.service.abstr.UUIDService;
import org.athend.axon.simple.service.abstr.UserService;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional
public class UserServiceImpl implements UserService {
	
	@Autowired private UUIDService uuidService;
	@Autowired private CommandBus commandBus;
	
	@Override
    //@Transactional
	public UUID createUser(String username) {
		UUID userId = uuidService.randomUUID();
		commandBus.dispatch(new GenericCommandMessage<>(new UserCreateCommand(
                userId,
                username
            )));
		return userId;
	}

	@Override
    //@Transactional
	public void lockUser(UUID userId) {
		commandBus.dispatch(new GenericCommandMessage<>(new UserLockCommand(userId)));
	}

}
