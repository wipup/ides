package ports.soc.ides.interceptor;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import ports.soc.ides.util.FacesUtils;

/**
 * Taken from: https://dzone.com/articles/session-timeout-handling-jsf
 * @author WIPU
 *
 */
public class RestoreViewPhaseListener implements PhaseListener {

	private static final long serialVersionUID = -3356058963381132745L;

	private static final Logger log = LogManager.getRootLogger();

	@Override
	public void afterPhase(PhaseEvent event) {
	}

	@Override
	public void beforePhase(PhaseEvent event) {
		
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx == null) {
			return;
		}
		
		ExternalContext etx = ctx.getExternalContext();
		if (etx == null) {
			return;
		}

		Object reqObj = ctx.getExternalContext().getRequest();
		if (!(reqObj instanceof HttpServletRequest)) {
			return;
		}
		HttpServletRequest req = (HttpServletRequest) reqObj;

		if (req.isRequestedSessionIdValid()) {
			return;
		}

		PrimeFaces pf = PrimeFaces.current();
		if (pf == null) {
			return;
		}
		if (!pf.isAjaxRequest()) {
			return;
		}
		
		try {
			log.info("Received Ajax request having invalid session id");
			FacesUtils.handleInvalidSessionId(FacesContext.getCurrentInstance());
		} catch (Exception e) {
			log.error("Error redirect at phase listener beforePhase " + getPhaseId(), e);
		}
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}

}
