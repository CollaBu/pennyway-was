package kr.co.pennyway.infra.config;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.co.pennyway.infra.common.importer.PennywayInfraConfig;
import kr.co.pennyway.infra.common.properties.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;

@EnableRabbit
@RequiredArgsConstructor
public class MessageBrokerConfig implements PennywayInfraConfig {
    private final RabbitMQProperties rabbitMQProperties;

    @Bean
    public Queue chatQueue() {
        return new Queue(rabbitMQProperties.getQueue(), true);
    }

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(rabbitMQProperties.getExchange());
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(chatQueue)
                .to(chatExchange)
                .with(rabbitMQProperties.getRoutingKey());
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
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
