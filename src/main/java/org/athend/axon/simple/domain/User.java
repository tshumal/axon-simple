package org.athend.axon.simple.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.athend.axon.simple.events.UserCreatedEvent;
import org.athend.axon.simple.events.UserLockedEvent;
import org.athend.axon.simple.events.UserUnlockedEvent;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateRoot;
import org.axonframework.eventhandling.EventHandler;

import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Getter
@NoArgsConstructor
@AggregateRoot
public class User {
    private static final long serialVersionUID = 1L;

    @AggregateIdentifier
    private UUID userId;

    private boolean locked;

    public User(UUID userId, String username) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("username must not be blank");
        }
        apply(new UserCreatedEvent(userId, username));
    }

    public void lock() {
        if (!locked) {
            apply(new UserLockedEvent(userId));
        }
    }

    public void unlock() {
        if (locked) {
            apply(new UserUnlockedEvent(userId));
        }
    }

    @EventHandler
    public void handle(UserCreatedEvent userCreatedEvent) {
        this.userId = userCreatedEvent.getUserId();
    }

    @EventHandler
    public void handle(UserLockedEvent userLockedEvent) {
        this.locked = true;
    }

    @EventHandler
    public void handle(UserUnlockedEvent userUnlockedEvent) {
        this.locked = false;
    }
}
