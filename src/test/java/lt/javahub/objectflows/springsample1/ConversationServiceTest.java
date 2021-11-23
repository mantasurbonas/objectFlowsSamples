package lt.javahub.objectflows.springsample1;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;

import lt.visma.javahub.flows.engine.ICorrelationIDFactory;
import lt.visma.javahub.flows.engine.impl.InMemoryRepository;
import lt.visma.javahub.flows.engine.impl.SimpleCorrelationIDFactory;

@ActiveProfiles("test")
@RunWith(MockitoJUnitRunner.class)
public class ConversationServiceTest {

	@Mock
	private EmailService emailService;
	
	@Mock
	private SampleConversationClient sampleConversationClient;

	@Spy
	private ICorrelationIDFactory correlationIDFactory = new SimpleCorrelationIDFactory();
	
	@Spy
	private InMemoryRepository repo = new InMemoryRepository();
	
	@Spy
	@InjectMocks
	private SampleConversationService conversationService;

	@Before
	public void prepare() {
	    MockitoAnnotations.initMocks(this);
	    conversationService.init();
	}
	
	@After
	public void cleanup() {
		// repo.clear();
	}
	
	@Test
	public void testHappyPath() {
		doReturn("DUMMY_CORRELATION_ID")
			.when(correlationIDFactory).newCorrelationID();
		
		doReturn(Arrays.asList(new EmailDTO()
								.setSubject("RE: Hello!")
								.setFrom("jonas@jonas.lt")
								.setBody("ping back!")
								.setCorrelationId("DUMMY_CORRELATION_ID")))
				.when(emailService).fetchEmails();
		
		conversationService.startConversationWith("jonas@jonas.lt");
				
		conversationService.continueConversations();
		
		verify(emailService).sendEmail(eq("jonas@jonas.lt"), anyString(), anyString(), eq("DUMMY_CORRELATION_ID"));
		verify(emailService, times(1)).fetchEmails();
		verify(sampleConversationClient, times(1)).onSuccess(eq("jonas@jonas.lt"));
	}

	@Test
	public void shouldDetectWrongRespondent() {
		doReturn("DUMMY_CORRELATION_ID")
			.when(correlationIDFactory).newCorrelationID();
		
		doReturn(Arrays.asList(new EmailDTO()
								.setSubject("RE: Hello!")
								.setFrom("petras@petras.lt")
								.setBody("ping back!")
								.setCorrelationId("DUMMY_CORRELATION_ID")))
				.when(emailService).fetchEmails();
		
		conversationService.startConversationWith("jonas@jonas.lt");
				
		conversationService.continueConversations();
		
		verify(emailService).sendEmail(eq("jonas@jonas.lt"), anyString(), anyString(), eq("DUMMY_CORRELATION_ID"));
		verify(emailService, times(1)).fetchEmails();
		verify(sampleConversationClient, times(1)).onFailure("jonas@jonas.lt");
	}
	
	@Test
	public void testTimeouts() {
		doReturn("SOME_DUMMY_CORRELATION_ID")
			.when(correlationIDFactory).newCorrelationID();
		
		conversationService.startConversationWith("petras@petras.lt");
		
		conversationService.continueConversations();
		
		long TWO_WEEKS_msec = 1000 * 60 * 60 * 24 * 7 * 2 + new Date().getTime();
		conversationService.engine.onTimer("SOME_DUMMY_CORRELATION_ID", TWO_WEEKS_msec);
		verify(sampleConversationClient, times(1)).onTimeout(eq("petras@petras.lt"));
	}
	
}
