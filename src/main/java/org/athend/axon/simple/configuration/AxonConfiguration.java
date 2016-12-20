package org.athend.axon.simple.configuration;

import org.athend.axon.simple.domain.User;
import org.athend.axon.simple.domain.UserLockSaga;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.model.Repository;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SimpleEventHandlerInvoker;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.saga.AbstractSagaManager;
import org.axonframework.eventhandling.saga.AnnotatedSagaManager;
import org.axonframework.eventhandling.saga.repository.AnnotatedSagaRepository;
import org.axonframework.eventhandling.saga.repository.SagaStore;
import org.axonframework.eventhandling.saga.repository.jpa.JpaSagaStore;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.quartz.QuartzEventScheduler;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.CachingEventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.config.CommandHandlerSubscriber;
import org.axonframework.spring.config.annotation.AnnotationCommandHandlerBeanPostProcessor;
import org.axonframework.spring.eventsourcing.SpringPrototypeAggregateFactory;
import org.axonframework.spring.jdbc.SpringDataSourceConnectionProvider;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.axonframework.spring.saga.SpringResourceInjector;
import org.quartz.SchedulerException;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class AxonConfiguration {

    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    private EventStore eventStore;

    @Autowired
    private SagaStore<Object> sagaStore;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;


    @Autowired
    private User user;

    @Bean
    JacksonSerializer axonJsonSerializer() {
        return new JacksonSerializer();
    }

   @Bean
    public SpringDataSourceConnectionProvider springDataSourceConnectionProvider() {
        return new SpringDataSourceConnectionProvider(dataSource);
    }

   @Bean
   public SimpleEntityManagerProvider simpleEntityManagerProvider(){
       return new SimpleEntityManagerProvider(entityManager);
   }

    @Bean
    public JpaEventStorageEngine eventStorageEngine() {
        return new JpaEventStorageEngine(new SimpleEntityManagerProvider(entityManager));
    }

    @Bean
    public AnnotationAwareAspectJAutoProxyCreator annotationAwareAspectJAutoProxyCreator() {
        return new AnnotationAwareAspectJAutoProxyCreator();
    }

    @Bean
    public EventStore eventStore() {
        return new EmbeddedEventStore(eventStorageEngine());
    }

    @Bean
    public CommandBus commandBus() {
        SimpleCommandBus commandBus = new SimpleCommandBus();
        commandBus.registerHandlerInterceptor(new BeanValidationInterceptor<>());
        return commandBus;

    }

    @Bean
    public AnnotationCommandHandlerBeanPostProcessor annotationCommandHandlerBeanPostProcessor() {
        return new AnnotationCommandHandlerBeanPostProcessor();
    }

    @Bean
    public CommandHandlerSubscriber commandHandlerSubscriber() {
        return new CommandHandlerSubscriber();
    }


    @Bean
    @Scope("prototype")
    public User user() {
        return new User();
    }

    @Bean
    public EventBus eventBus() {
        return eventStore();
    }

    @Bean
    public SpringResourceInjector resourceInjector() {
        return new SpringResourceInjector();
    }

    @Bean
    public SagaStore<Object> sagaStore(DataSource dataSource) {
        return new JpaSagaStore(simpleEntityManagerProvider());
    }

    @Bean
    public SchedulerFactoryBean scheduleFactoryBean() {
        return new SchedulerFactoryBean();
    }

   @Bean
   SpringTransactionManager springTransactionManager(){
       return new SpringTransactionManager(platformTransactionManager);
   }


    @Bean
    public EventScheduler eventScheduler(SpringTransactionManager springTransactionManager, EventBus eventBus) throws SchedulerException {
        QuartzEventScheduler eventScheduler = new QuartzEventScheduler();
        eventScheduler.setEventBus(eventStore);
        eventScheduler.setTransactionManager(springTransactionManager());
        eventScheduler.setScheduler(scheduleFactoryBean().getScheduler());
        eventScheduler.initialize();
        return eventScheduler;
    }

    @Bean
    public Repository<User> userRepository() {
        CachingEventSourcingRepository<User> repository = new CachingEventSourcingRepository<>(
            userAggregateFactory(),
            eventStore,
            new WeakReferenceCache());
        return repository;
    }

    @Bean
    public AggregateFactory<User> userAggregateFactory() {
        SpringPrototypeAggregateFactory<User> aggregateFactory = new SpringPrototypeAggregateFactory<>();
        aggregateFactory.setPrototypeBeanName("user");
        return aggregateFactory;
    }


    @Bean
    public AnnotatedSagaRepository<UserLockSaga> sagaRepository() {
        return new AnnotatedSagaRepository<>(UserLockSaga.class, sagaStore, resourceInjector());
    }

    @Bean
    public AbstractSagaManager<UserLockSaga> sagaManager() {
        return new AnnotatedSagaManager<>(
            UserLockSaga.class,
            sagaRepository()
        );
    }

@Bean
    SubscribingEventProcessor eventProcessor(){

    SubscribingEventProcessor eventProcessor = new SubscribingEventProcessor("eventProcessor",
        new SimpleEventHandlerInvoker(
            user),
        eventBus());
    eventProcessor.start();
    return eventProcessor;

}

    @Bean
    public EventProcessor sagaEventProcessor() {
        SubscribingEventProcessor eventProcessor = new SubscribingEventProcessor(
            "sagaEventProcessor",
            sagaManager(), //new SimpleEventHandlerInvoker(userLockSaga)
            eventStore);
        eventProcessor.start();

        return eventProcessor;
    }
}


