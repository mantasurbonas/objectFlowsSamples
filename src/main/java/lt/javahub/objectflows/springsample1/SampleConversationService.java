package lt.javahub.objectflows.springsample1;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lt.javahub.objectflows.utils.Log4JLoggerImpl;
import lt.visma.javahub.flows.Flow;
import lt.visma.javahub.flows.engine.FlowsEngine;
import lt.visma.javahub.flows.engine.ICorrelationIDFactory;
import lt.visma.javahub.flows.engine.IFlowsRepository;
import lt.visma.javahub.flows.engine.impl.AlwaysPersistStrategy;
import lt.visma.javahub.flows.engine.impl.SimpleDependencyFactory;

/****
 * example of Spring dependency-injectable component with durable long-running multi-step workflows:
 * 
 * sends email to a specified recipient, 
 * and then waits for up to a minute for an email reply with a subject ending in "Hello!" 
 * 
 * in case of timeout or error prints appropriate messages.
 * 
 * @author mantas.urbonas
 *
 */

@Component
public class SampleConversationService{
		
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private SampleConversationClient sampleConversationClient;
	
	@Autowired
	private IFlowsRepository repo;
	
	@Autowired
	private ICorrelationIDFactory correlationIDFactory;

	
	FlowsEngine engine;

	@PostConstruct
	public void init() {
		 engine = new FlowsEngine()
					.setLogger(new Log4JLoggerImpl())
					.setRepository(repo)
					.setCorrelationIDFactory(correlationIDFactory)
					.setDependencyFactory(
							new SimpleDependencyFactory()
									.registerService(emailService)
									.registerService(sampleConversationClient))
					.setPersistanceStrategy(new AlwaysPersistStrategy());
	}
	
	public void startConversationWith(String recipientEmail) {
		Flow conversation = SampleConversationFactory.createConversation( recipientEmail );
		engine.startNewFlow(conversation);
	}
	
	public void continueConversations() {
		processEmails(emailService.fetchEmails(), engine);
		
		engine.onTimer();
	}
	
	private void processEmails(List<EmailDTO> emails, FlowsEngine engine) {
		for (EmailDTO email: emails) {
			String correlationId = email.getCorrelationId();
			
			if (! correlationIDFactory.isValid(correlationId) )
				continue;
			
			engine.onEvent( correlationId, "email", email );
		}
	}

}
