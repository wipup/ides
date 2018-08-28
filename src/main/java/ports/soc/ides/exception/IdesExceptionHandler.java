package ports.soc.ides.exception;

import java.util.Iterator;

import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.util.FacesUtils;

/**
 * https://stackoverflow.com/a/28614033
 *
 */
public class IdesExceptionHandler extends ExceptionHandlerWrapper {

	protected static final Logger log = LogManager.getRootLogger();

	public IdesExceptionHandler(ExceptionHandler wrapped) {
		super(wrapped);
	}

	private static boolean isInterestedException(Throwable t) {
		if (t instanceof ViewExpiredException) {
			return true;
		}
		if (t instanceof IllegalStateException) {
			return true;
		}
		return false;
	}

	private void handleViewExpiredException() {
		try {
			FacesUtils.handleInvalidSessionId(FacesContext.getCurrentInstance());
		} catch (Exception e) {
			log.fatal("Error while handling view expired exception", e);
		}
	}

	@Override
	public void handle() {
		Iterator<ExceptionQueuedEvent> it = getUnhandledExceptionQueuedEvents().iterator();
		while (it.hasNext()) {
			ExceptionQueuedEvent ex = it.next();
			log.error("IdesExceptionHandler handling " + ex.getContext().getException().getMessage());

			ExceptionQueuedEventContext context = ex.getContext();
			Throwable t = context.getException();

			if (isInterestedException(t)) {
				handleViewExpiredException();
				it.remove();
			} else {
				Throwable cause = t;
				while (cause != null) {
					log.error("Exception caused by: " + cause.getClass().getName() + ", message=" + cause.getMessage());
					if (isInterestedException(cause)) {
						handleViewExpiredException();
						it.remove();
						break;
					}
					cause = cause.getCause();
				}
			}
		}
		getWrapped().handle();
	}

}
