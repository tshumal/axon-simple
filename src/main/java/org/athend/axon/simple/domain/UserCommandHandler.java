package org.athend.axon.simple.domain;

import lombok.Setter;
import org.athend.axon.simple.commands.UserCreateCommand;
import org.athend.axon.simple.commands.UserLockCommand;
import org.athend.axon.simple.commands.UserUnlockCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.Aggregate;
import org.axonframework.commandhandling.model.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class UserCommandHandler {
    @Setter
    @Autowired
    @Qualifier("userRepository")
    private Repository<User> userRepository;

    @CommandHandler
    public void handle(final UserCreateCommand userCreateCommand) throws Exception {
        final Aggregate<User> userAggregate = userRepository.newInstance(() -> new User(
            userCreateCommand.getUserId(),
            userCreateCommand.getUsername()));
    }

    @CommandHandler
    public void handle(final UserLockCommand userLockCommand) {
        Aggregate<User> userAggregate = userRepository.load(userLockCommand.getUserId().toString());
        userAggregate.execute(user -> {
            user.lock();
        });
    }

    @CommandHandler
    public void handle(final UserUnlockCommand userUnlockCommand) {
        Aggregate<User> userAggregate = userRepository.load(userUnlockCommand.getUserId().toString());
        userAggregate.execute(user -> {
            user.unlock();
        });
    }
}
