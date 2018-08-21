package ports.soc.ides.controller;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import ports.soc.ides.util.DateTimeUtils;
import ports.soc.ides.util.FacesUtils;
import ports.soc.ides.util.IdesUtils;

/**
 * An abstract class of JSF CDI backing bean containing all necessary methods of
 * every view-controller bean
 * 
 * @author WIPU
 *
 */
public abstract class AbstractIdesController implements Serializable {

	public static final String ID_COMPONENT_MESSAGE = "message";
	public static final String PARAM_MODAL_VAR = "modalVarName";

	private static final long serialVersionUID = 5745665635495562404L;

	protected static final Logger log = LogManager.getRootLogger();

	private boolean updateGrowl = true;

//	private PropertyResourceBundle idesMessage;

	public void addAllExceptionCausesToMessage(Throwable ex) {
		if (ex == null) {
			return;
		}
		do {
			String message = ex.getMessage();
			addMessageError("Caused by", message);
			ex = ex.getCause();
		} while (ex != null);
	}
	
	protected void executeJavaScript(String script) {
		PrimeFaces.current().executeScript(script);
	}
	
	protected void showRequestedModal() {
		//Fix an issue when the client could not connect to the server but still show a modal  
		String modalName = getParameter(PARAM_MODAL_VAR);
		if (!IdesUtils.isEmpty(modalName)) {
			executeJavaScript("PF('" + modalName.trim() + "').show()");
		}
	}

	/**
	 * Retrieve backing bean using EL resolver and Faces context
	 * 
	 * @param clazz
	 * @return
	 */
	public <T extends AbstractIdesController> T getNamedController(Class<T> clazz) {
		return FacesUtils.getNamedController(clazz);
	}

	public void addMessageError(String summary, String detail) {
		addMessage(summary, detail, FacesMessage.SEVERITY_ERROR);
	}

	public void addMessageWarn(String summary, String detail) {
		addMessage(summary, detail, FacesMessage.SEVERITY_WARN);
	}

	public void addMessageFatal(String summary, String detail) {
		addMessage(summary, detail, FacesMessage.SEVERITY_FATAL);
	}

	public void addMessageInfo(String summary, String detail) {
		addMessage(summary, detail, FacesMessage.SEVERITY_INFO);
	}

	/**
	 * Send notification message to PrimeFaces's component on front-end
	 * 
	 * @param summary
	 *            Header string of the notification
	 * @param detail
	 *            Detail string of the message
	 * @param severity
	 *            Severity of the message e.g. Fatal, Warning or Error
	 */
	protected void addMessage(String summary, String detail, FacesMessage.Severity severity) {
		if (severity == FacesMessage.SEVERITY_INFO) {
			log.debug("Add message(" + String.valueOf(severity) + ")[header=" + summary + ", detail=" + detail + "]");
		} else {
			log.info("Add message(" + String.valueOf(severity) + ")[header=" + summary + ", detail=" + detail + "]");
		}

		FacesMessage message = new FacesMessage(severity, summary, detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
		if (isUpdateGrowl()) {
			updateMessage();
		}
		FacesUtils.stopLoggingSessionId();
	}

	public void updateMessage() {
		PrimeFaces.current().ajax().update(ID_COMPONENT_MESSAGE);
	}

	protected String findParameterEndingWith(String name) {
		Map<String, String> params = getParameterMap();
		for (String key : params.keySet()) {
			if (key.endsWith(name)) {
				return params.get(key);
			}
		}
		return null;
	}

	protected ResourceBundle getBundledResource(String name) {
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			return FacesContext.getCurrentInstance().getApplication().getResourceBundle(ctx, name);
		} catch (Exception e) {
			log.error("Error while retreiving bundled resource name: " + name);
		}
		return null;
	}
	
	protected String getBundledMessage(String key) {
		try {
			return getBundledResource("msg").getString(key);
		} catch (Exception e) {
			log.error("Bundled message of key " + key + " not found", e);
		}
		return key;
	}

	/**
	 * Get an AJAX parameter from the front-end request
	 * 
	 * @param name
	 *            Name of the AJAX parameter
	 * @return
	 */
	protected String getParameter(String name) {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
	}

	protected int getIntParameter(String name) throws NumberFormatException {
		return Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name));
	}

	protected long getLongParameter(String name) throws NumberFormatException {
		return Long.parseLong(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name));
	}

	/**
	 * Get all AJAX parameters from the front-end request
	 * 
	 * @return
	 */
	protected Map<String, String> getParameterMap() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
	}

	/**
	 * For debugging only
	 */
	protected void printAllRequestParameters() {
		log.debug("=== printAllRequestParameters ===");
		Map<String, String> params = getParameterMap();
		for (String k : params.keySet()) {
			log.debug(k + " = " + params.get(k));
		}
		log.debug("=================================");
	}

	public boolean isUpdateGrowl() {
		return updateGrowl;
	}

	public void setAutoUpdateOverlayNotification(boolean updateGrowl) {
		this.updateGrowl = updateGrowl;
	}
	
	public String formatDateTime(LocalDateTime dt) {
		return DateTimeUtils.formatDateTime(dt);
	}
	
	public String formatLongDateTime(LocalDateTime dt) {
		return DateTimeUtils.formatLongDateTime(dt);
	}
	

}
