package org.athend.axon.simple;

import org.athend.axon.simple.commands.UserUnlockCommand;
import org.athend.axon.simple.domain.UserLockSaga;
import org.athend.axon.simple.events.UserLockedEvent;
import org.athend.axon.simple.events.UserUnlockedEvent;
import org.axonframework.test.saga.AnnotatedSagaTestFixture;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;



/**
 * Created by lingani on 2016/12/07.
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = {Application.class})
@PrepareForTest({UUID.class})
@RunWith(PowerMockRunner.class)
public class TestUserLockSaga {

    private AnnotatedSagaTestFixture fixture;
    private UUID uuidMock;

    @Before
    public void setUp() throws Exception {
        fixture = new AnnotatedSagaTestFixture<>(UserLockSaga.class);
        fixture.registerResource(new DateTime());

        uuidMock = PowerMockito.mock(UUID.class);
        PowerMockito.mockStatic(UUID.class);
        PowerMockito.when(UUID.randomUUID()).thenReturn(uuidMock);
    }


    @Test
    public void when_lockingUser_expect_SagaIsInvoked() throws Exception {
        fixture.givenAPublished(new UserLockedEvent(uuidMock))
            .whenPublishingA(new UserUnlockedEvent(uuidMock))
            .expectDispatchedCommandsEqualTo(new UserUnlockCommand(uuidMock));
    }
}
