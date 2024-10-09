package kr.co.pennyway.infra.config;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.co.pennyway.infra.client.broker.MessageBrokerAdapter;
import kr.co.pennyway.infra.common.importer.PennywayInfraConfig;
import kr.co.pennyway.infra.common.properties.ChatExchangeProperties;
import kr.co.pennyway.infra.common.properties.RabbitMqProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@EnableRabbit
@RequiredArgsConstructor
@EnableConfigurationProperties({ChatExchangeProperties.class, RabbitMqProperties.class})
public class MessageBrokerConfig implements PennywayInfraConfig {
    private final RabbitMqProperties rabbitMqProperties;
    private final ChatExchangeProperties chatExchangeProperties;

    @Bean
    public Queue chatQueue() {
        return new Queue(chatExchangeProperties.getQueue(), true);
    }

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(chatExchangeProperties.getExchange());
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(chatQueue)
                .to(chatExchange)
                .with(chatExchangeProperties.getRoutingKey());
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
}
