package lt.javahub.objectflows.springsample1;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lt.javahub.objectflows.utils.FileRepo;
import lt.visma.javahub.flows.engine.ICorrelationIDFactory;
import lt.visma.javahub.flows.engine.IFlowsRepository;
import lt.visma.javahub.flows.engine.impl.SimpleCorrelationIDFactory;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class SpringSample1Config {

	@Bean("repo")
	public IFlowsRepository getRepo() {
		return new FileRepo();
	}
	
	@Bean
	public ICorrelationIDFactory getCorrelationIDFactory() {
		return SimpleCorrelationIDFactory.getInstance();
	}
}
