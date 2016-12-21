package org.athend.axon.simple.domain;

import lombok.Setter;
import org.athend.axon.simple.commands.UserUnlockCommand;
import org.athend.axon.simple.events.UserLockedEvent;
import org.athend.axon.simple.events.UserUnlockedEvent;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.ScheduleToken;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.axonframework.eventhandling.saga.SagaLifecycle.end;

@Saga
public class UserLockSaga {
    // unlock after 5 secs
    //public static final Long UNLOCK_AFTER_MILLIS = new Long(DateTimeConstants.MILLIS_PER_SECOND * 5);

    private static final Duration SCHEDULE_DURATION = Duration.ofSeconds(10);

    private static final long serialVersionUID = 1L;

    @Autowired
    @Setter
    private   transient CommandBus commandBus;

    @Autowired
    @Setter
    private  transient EventScheduler eventScheduler;

    private  transient ScheduleToken userUnlockToken;

    @SagaEventHandler(associationProperty = "userId")
    @StartSaga
    public void handle(UserLockedEvent event, EventMessage<UserLockedEvent> message) {
        System.out.println("Event ID " + event.getUserId().toString());

        if (userUnlockToken != null) {
            eventScheduler.cancelSchedule(userUnlockToken);
        }
        // scheduling a new event to be fired at the specified time. The event handler for this event is only in
        // the saga and would then trigger a UserUnlockCommand

        //Duration duration = getUnlockDuration(eventTime);
       // long seconds = duration.getSeconds();
        this.userUnlockToken = eventScheduler.schedule(SCHEDULE_DURATION, new GenericEventMessage<>(new UserUnlockedEvent(event.getUserId())));

       //commandBus.dispatch(new GenericCommandMessage<>(new UserUnlockCommand(event.getUserId())));
    }

    /*@SagaEventHandler(associationProperty = "userId")
    @EndSaga
    public void handle(final UserUnlockSagaEvent unlockSagaEvent) {
        System.out.println("SagaEventHandler Saga ending, Unlocking the User by issuing the UserUnlockCommand ...");
        commandBus.dispatch(new GenericCommandMessage<>(new UserUnlockCommand(unlockSagaEvent.getUserId())));
        //end();
    }*/

    /**
     * Also, if some external action unlocks the user, the saga should stop.
     *
     * @param userUnlockedEvent
     */
    @SagaEventHandler(associationProperty = "userId")
    public void handle(final UserUnlockedEvent userUnlockedEvent) {
        if (this.userUnlockToken != null) {
            eventScheduler.cancelSchedule(userUnlockToken);
        }
        else{
            commandBus.dispatch(new GenericCommandMessage<>(new UserUnlockCommand(userUnlockedEvent.getUserId())));
            end();
        }
        //end();
    }

   // private Duration getUnlockDuration(DateTime eventTime) {
   //     return Duration.ofMillis(UNLOCK_AFTER_MILLIS);
   // }
}
