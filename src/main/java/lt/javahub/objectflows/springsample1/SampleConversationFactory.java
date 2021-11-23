package lt.javahub.objectflows.springsample1;

import static lt.visma.javahub.flows.Flows.condition;
import static lt.visma.javahub.flows.Flows.eq;
import static lt.visma.javahub.flows.Flows.first;
import static lt.visma.javahub.flows.Flows.printVar;
import static lt.visma.javahub.flows.Flows.println;
import static lt.visma.javahub.flows.Flows.set;
import static lt.visma.javahub.flows.Flows.waitFor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lt.visma.javahub.flows.Flow;
import lt.visma.javahub.flows.Result;
import lt.visma.javahub.flows.ResultStatus;

public class SampleConversationFactory {


	private static Logger log = LogManager.getLogger(SampleConversationFactory.class);
	
	public static Flow createConversation(String emailTo) {
		return first( set("recipient", emailTo) )
				
				.then(println("about to send an email to ") )
				.then(printVar("recipient"))
				.then(println(""))
				.then( condition().on( eq("recipient", "jonas@jonas.lt"), println("(talking to someone I know!)"))
								  .otherwise(println("(talking to someone I don't know)"))
					 )
				
				.then(new SendGreetingsEmailFlow())
				
				.then(waitFor()
						.onEvent("email", new ProcessGreetingsResponseFlow().setFrom(emailTo))
						.onTimeout(1000 * 60, first(println("timeouted! no response received within a minute")).then(new TimeoutedFlow()))
					 )
				
				.then(println("flow ended"))
				.then( new SucceededFlow())
				
				.onError(Exception.class, new FailedFlow());
	}

	public static class SendGreetingsEmailFlow extends Flow{

		@Override
		protected Result execute() {
			String recipient = context.getString("recipient");
			getDependencies().getService(SampleConversationClient.class).onStarted(recipient);
			
			EmailService emailService = dependencies.getService(EmailService.class);
			emailService.sendEmail(recipient, "Hello!", "This is a hello message", context.getCorrelationID());
			log.info("just finished SendGreetingsEmail flow");
			
			return Result.COMPLETED;
		}
	}
	
	public static class ProcessGreetingsResponseFlow extends Flow{

		private String recipient;
		
		public ProcessGreetingsResponseFlow setFrom(String from) {
			this.recipient = from;
			return this;
		}
		
		@Override
		protected Result execute() {
			EmailDTO email = context.get("email", EmailDTO.class);
			
			log.info("Email received: from ["+email.getFrom()+"] subj: '"+email.getSubject()+"' text: '"+ email.getBody()+"'");
			
			if (!email.getSubject().endsWith("Hello!"))
				return new Result(new RuntimeException("Expected subject 'RE: Hello!', actual: '"+email.getSubject()+"'"));
			
			if (!email.getFrom().equalsIgnoreCase(recipient))
				return new Result(new RuntimeException("Expected response from "+recipient+", actually received from "+email.getFrom()));
			
			log.info("Okay: greeting response as expected.");
			
			return Result.COMPLETED;
		}
	}

	public static class SucceededFlow extends Flow{
		@Override
		protected Result execute() {
			String recipient = context.getString("recipient");
			getDependencies().getService(SampleConversationClient.class).onSuccess(recipient);
			
			return Result.COMPLETED;
		}
	}

	public static class TimeoutedFlow extends Flow{
		@Override
		protected Result execute() {
			String recipient = context.getString("recipient");
			getDependencies().getService(SampleConversationClient.class).onTimeout(recipient);
			
			return new Result(ResultStatus.FAILED);
		}
	}

	public static class FailedFlow extends Flow{
		@Override
		protected Result execute() {
			String recipient = context.getString("recipient");
			getDependencies().getService(SampleConversationClient.class).onFailure(recipient);
			
			return new Result(ResultStatus.FAILED);
		}
	}
	
}
