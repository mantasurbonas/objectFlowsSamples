package lt.javahub.objectflows.sample2;

import static lt.visma.javahub.flows.Flows.*;
import static lt.visma.javahub.flows.Flows.print;
import static lt.visma.javahub.flows.Flows.printVar;
import static lt.visma.javahub.flows.Flows.println;
import static lt.visma.javahub.flows.Flows.readln;

import java.util.Collection;

import lt.javahub.objectflows.utils.FileRepo;
import lt.javahub.objectflows.utils.Log4JLoggerImpl;
import lt.visma.javahub.flows.engine.FlowsEngine;
import lt.visma.javahub.flows.engine.IFlowsRepository;
import lt.visma.javahub.flows.engine.impl.AlwaysPersistStrategy;
import lt.visma.javahub.flows.impl.SequentialFlow;

/***
 * example of a durable long-running multistep workflow:
 * 
 * if no existing workflow found, starts a new one.
 * 
 * else, runs the previously unfinished workflow(s).
 * 
 * 
 * @author mantas.urbonas
 *
 */
public class Sample2 {
	
	public static void main(String... args) throws Exception {	
		IFlowsRepository flowsRepository = new FileRepo();
		FlowsEngine engine = new FlowsEngine()
									.setLogger(new Log4JLoggerImpl())
									.setRepository(flowsRepository)
									.setPersistanceStrategy(new AlwaysPersistStrategy())
									;
		
		Collection<String> correlationIDs = flowsRepository.listAll();
		if (correlationIDs.isEmpty())
			engine.startNewFlow( createNewFlow() );
		else
			engine.onTimer();
	}
	
	private static SequentialFlow createNewFlow() {
		return first( println("hello, user") )
			.then( println("**** you can terminate this program anytime you want") )
			.then( println("**** but it will continue exactly where you've finished last time.") )
			.then( readln("Step 1/3 what's your first name? :", "name") )
			.then( condition().on(eq("name", "mantas"), println("Hi man"))
							  .on(eq("name", "jonas"), println("I know you too"))
						      .otherwise(print("nice to meet you, stranger")))
			.then( readln("Step 2/3 what's your last name? :", "lastName"))
			.then( readln("Step 3/3 how old are you?", "age") )
			.then( print("your name is "))
			.then( printVar("name"))
			.then( print(", your last name is "))
			.then( printVar("lastName"))
			.then( print(", your age is "))
			.then( printVar("age"))
			.then( println("") )
			.then( condition().on(eq("age", "100"), println("you are lying"))
							  .otherwise(println("done now")));
	}
 
}
