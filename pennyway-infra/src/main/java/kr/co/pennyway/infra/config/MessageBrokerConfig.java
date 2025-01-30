package kr.co.pennyway.infra.config;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.co.pennyway.infra.client.broker.MessageBrokerAdapter;
import kr.co.pennyway.infra.common.event.ChatRoomJoinEventHandler;
import kr.co.pennyway.infra.common.event.SpendingChatShareEventHandler;
import kr.co.pennyway.infra.common.importer.PennywayInfraConfig;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.infra.common.properties.ChatJoinEventExchangeProperties;
import kr.co.pennyway.infra.common.properties.RabbitMqProperties;
import kr.co.pennyway.infra.common.properties.SpendingChatShareExchangeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Slf4j
@EnableRabbit
@RequiredArgsConstructor
@EnableConfigurationProperties({ChatExchangeProperties.class, ChatJoinEventExchangeProperties.class, RabbitMqProperties.class, SpendingChatShareExchangeProperties.class})
public class MessageBrokerConfig implements PennywayInfraConfig {
    private final RabbitMqProperties rabbitMqProperties;
    private final ChatExchangeProperties chatExchangeProperties;
    private final ChatJoinEventExchangeProperties chatJoinEventExchangeProperties;
    private final SpendingChatShareExchangeProperties spendingChatShareExchangeProperties;

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(chatExchangeProperties.getExchange());
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(chatExchangeProperties.getQueue(), true);
    }

    @Bean
    public Queue chatJoinEventQueue(ChatJoinEventExchangeProperties chatJoinEventExchangeProperties) {
        return new Queue(chatJoinEventExchangeProperties.getQueue(), true);
    }

    @Bean
    public Queue spendingChatShareQueue(SpendingChatShareExchangeProperties spendingChatShareExchangeProperties) {
        return new Queue(spendingChatShareExchangeProperties.getQueue(), true);
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(chatQueue)
                .to(chatExchange)
                .with(chatExchangeProperties.getRoutingKey());
    }

    @Bean
    public Binding chatJoinEventBinding(Queue chatJoinEventQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(chatJoinEventQueue)
                .to(chatExchange)
                .with(chatJoinEventExchangeProperties.getRoutingKey());
    }

    @Bean
    public Binding spendingShareEventBinding(Queue spendingChatShareQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(spendingChatShareQueue)
                .to(chatExchange)
                .with(spendingChatShareExchangeProperties.getRoutingKey());
    }

    @Bean
    public Module dateTimeModule() {
        return new JavaTimeModule();
    }

    @Bean
    public MessageConverter messageConverter(Module dateTimeModule) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.registerModule(dateTimeModule);

        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    @Primary
    public ConnectionFactory createConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();

        factory.setHost(rabbitMqProperties.getHost());
        factory.setUsername(rabbitMqProperties.getUsername());
        factory.setPassword(rabbitMqProperties.getPassword());
        factory.setPort(rabbitMqProperties.getPort());
        factory.setVirtualHost(rabbitMqProperties.getVirtualHost());
        factory.setRequestedHeartBeat(rabbitMqProperties.getRequestedHeartbeat());

        return factory;
    }

    @Bean
    @ConditionalOnProperty(prefix = "pennyway.rabbitmq", name = "validate-connection", havingValue = "true", matchIfMissing = false)
    ApplicationRunner connectionFactoryRunner(ConnectionFactory cf) {
        return args -> {
            try (Connection conn = cf.createConnection()) {
                log.info("RabbitMQ connection validated");
            } catch (Exception e) {
                log.error("Failed to validate RabbitMQ connection", e);
                throw e;
            }
        };
    }

    @Bean
    @ConditionalOnProperty(prefix = "pennyway.rabbitmq", name = "chat-join-event-listener", havingValue = "true", matchIfMissing = false)
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(10);

        factory.setErrorHandler(t -> log.error("An error occurred in the listener", t));
        factory.setAutoStartup(true);

        return factory;
    }

    @Bean
    public RabbitTemplate customRabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public RabbitMessagingTemplate customRabbitMessagingTemplate(RabbitTemplate rabbitTemplate) {
        return new RabbitMessagingTemplate(rabbitTemplate);
    }

    @Bean
    public MessageBrokerAdapter messageBrokerAdapter(RabbitMessagingTemplate rabbitMessagingTemplate) {
        return new MessageBrokerAdapter(rabbitMessagingTemplate);
    }

    @Bean
    public ChatRoomJoinEventHandler chatRoomJoinEventHandler(MessageBrokerAdapter messageBrokerAdapter, ChatExchangeProperties chatExchangeProperties, ChatJoinEventExchangeProperties chatJoinEventExchangeProperties) {
        return new ChatRoomJoinEventHandler(messageBrokerAdapter, chatExchangeProperties, chatJoinEventExchangeProperties);
    }

    @Bean
    public SpendingChatShareEventHandler spendingChatShareEventHandler(MessageBrokerAdapter messageBrokerAdapter, ChatExchangeProperties chatExchangeProperties, SpendingChatShareExchangeProperties spendingChatShareExchangeProperties) {
        return new SpendingChatShareEventHandler(messageBrokerAdapter, chatExchangeProperties, spendingChatShareExchangeProperties);
    }
}
