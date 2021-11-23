package lt.javahub.objectflows.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lt.visma.javahub.flows.engine.FlowsEngine;
import lt.visma.javahub.flows.engine.ILogger;

public class Log4JLoggerImpl implements ILogger{

	private static Logger log = LogManager.getLogger(FlowsEngine.class);
	
	@Override
	public void warn(String message) {
		log.warn(message);
	}

	@Override
	public void debug(String message) {
		log.debug(message);
	}

}
