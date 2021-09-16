package com.uci.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import com.uci.utils.kafka.ReactiveProducer;
import com.uci.utils.kafka.SimpleProducer;

import io.fusionauth.client.FusionAuthClient;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@Configuration
@ConfigurationProperties
//@PropertySources({
//    @PropertySource("classpath:test-application.properties"),
//    @PropertySource(value="file:config.properties", ignoreResourceNotFound = 
//        true)})
@TestPropertySource("classpath:test-application.properties")
public class ApplicationConfiguration{
	@Value("${campaign.url}")
    public String CAMPAIGN_URL;
    
	@Value("${campaign.admin.token}")
	private String CAMPAIGN_ADMIN_TOKEN;
	
	@Value("${fusionauth.url}")
	private String fusionAuthUrl;

	@Value("${fusionauth.key}")
	private String fusionAuthKey;

	@Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVERS;    
	
	@Bean
	public WebClient getWebClient() {
		return WebClient.builder().baseUrl(CAMPAIGN_URL).defaultHeader("admin-token", CAMPAIGN_ADMIN_TOKEN).build();
	}
	
	@Bean
	public FusionAuthClient getFusionAuthClient() {
		return new FusionAuthClient(fusionAuthKey, fusionAuthUrl);
	}
	
	@Bean
    public BotService botService() {
        return new BotService(getWebClient(), getFusionAuthClient());
    }
	
	@Bean
    public CampaignService campaignService() {
        return new CampaignService(getWebClient(), getFusionAuthClient());
    }
	
	@Bean
    Map<String, Object> kafkaProducerConfiguration() {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configuration.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        configuration.put(ProducerConfig.ACKS_CONFIG, "all");
        configuration.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        configuration.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
        return configuration;
    }
	
	@Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory(kafkaProducerConfiguration());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
	
	@Bean
    SimpleProducer kafkaSimpleProducer() {
        return new SimpleProducer(kafkaTemplate());
    }
}