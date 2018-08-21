package ports.soc.ides.exception;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IdesExceptionHandlerFactory extends ExceptionHandlerFactory {

	protected static final Logger log = LogManager.getRootLogger();

	public IdesExceptionHandlerFactory(ExceptionHandlerFactory wrapped) {
    	super(wrapped);
    }
	
	@Override
	public ExceptionHandler getExceptionHandler() {
		return new IdesExceptionHandler(getWrapped().getExceptionHandler());
	}

}
