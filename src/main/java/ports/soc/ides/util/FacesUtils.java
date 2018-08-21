package ports.soc.ides.util;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import ports.soc.ides.controller.AbstractIdesController;
import ports.soc.ides.exception.IdesException;
import ports.soc.ides.logger.LoggerConfigurationFactory;

public class FacesUtils {

	private static final Logger log = LogManager.getRootLogger();

	public static void randomError() {
		if (IdesUtils.randomIntValue(10)) {
			log.error("Test random error");
			throw new IdesException("Test Random Error");
		}
	}

	public static ResourceBundle getResourceBundle(String baseName, FacesContext context) {
		return context.getApplication().getResourceBundle(context, baseName);
	}
	
	/**
	 * Force adding sessionId to logger thread context
	 * Also if sessionId is incorrect, replace with the correct one 
	 */
	public static void startLoggingSessionId() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx == null) {
			return;
		}

		ExternalContext ex = ctx.getExternalContext();
		if (ex == null) {
			return;
		}

		String sessionId = ex.getSessionId(false);
		if (IdesUtils.isEmpty(sessionId)) {
			return;
		}
		
		String currentIpAddress = "";
		Object req = ex.getRequest();
		if (req instanceof HttpServletRequest) {
			HttpServletRequest httpReq = (HttpServletRequest) req;
			currentIpAddress = httpReq.getRemoteAddr();
		}
		
		if (!ThreadContext.containsKey(LoggerConfigurationFactory.LOG_SESSION_ATTRIBUTE_KEY)) {
			ThreadContext.put(LoggerConfigurationFactory.LOG_SESSION_ATTRIBUTE_KEY, sessionId);
			ThreadContext.put(LoggerConfigurationFactory.LOG_IP_ADDRESS_ATTRIBUTE_KEY, currentIpAddress);
			log.trace("Added session id to logger: " + sessionId);
			log.trace("User-Agent: " + ex.getRequestHeaderMap().get("User-Agent"));
			return;
		}

		String session = ThreadContext.get(LoggerConfigurationFactory.LOG_SESSION_ATTRIBUTE_KEY);
		if (!sessionId.equals(session)) {
			log.trace("Incorrect logged sessionId, replace new one");
			ThreadContext.put(LoggerConfigurationFactory.LOG_SESSION_ATTRIBUTE_KEY, sessionId);
			log.trace("Added session id to logger: " + sessionId);
			log.trace("User-Agent: " + ex.getRequestHeaderMap().get("User-Agent"));
		}
		
		String threadIpAddress = ThreadContext.get(LoggerConfigurationFactory.LOG_IP_ADDRESS_ATTRIBUTE_KEY);
		if (!currentIpAddress.equals(threadIpAddress)) {
			ThreadContext.put(LoggerConfigurationFactory.LOG_IP_ADDRESS_ATTRIBUTE_KEY, currentIpAddress);
		}
	}

	public static void stopLoggingSessionId() {
		if (ThreadContext.containsKey(LoggerConfigurationFactory.LOG_SESSION_ATTRIBUTE_KEY)) {
			ThreadContext.remove(LoggerConfigurationFactory.LOG_SESSION_ATTRIBUTE_KEY);
		}
	}

	public static void redirectToWelcomePage(boolean responseComplete) throws IOException {
		FacesContext fctx = FacesContext.getCurrentInstance();
		ExternalContext ectx = fctx.getExternalContext();

		String reqPath = ectx.getRequestContextPath();
		FacesUtils.redirectTo(reqPath);
		if (responseComplete && !ectx.isResponseCommitted()) {
			fctx.responseComplete();
		}
	}

	public static void forceClearCache(HttpServletResponse resp) {
		if (resp.isCommitted()) {
			log.trace("response already committed, no cache clearing added");
			return;
		}
		
		resp.addHeader("Pragma", "no-cache"); // HTTP 1.0.
		resp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		resp.setDateHeader("Expires", 0); // Proxies.
	}

	/**
	 * https://stackoverflow.com/questions/49547/how-to-control-web-page-caching-across-all-browsers
	 * 
	 * @param facesContext
	 */
	public static void forceClearCache() {
		forceClearCache((HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse());
	}

	public static void redirectTo(String url) throws IOException {
		ExternalContext etx = FacesContext.getCurrentInstance().getExternalContext();
		
		if (etx.isResponseCommitted()) {
			return;
		}
		etx.redirect(url);
	}

	public static void redirectToWelcomePage() throws IOException {
		redirectToWelcomePage(true);
	}

	/**
	 * Retrieve backing bean using EL resolver in case {@link Inject} is not
	 * preferred
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T extends AbstractIdesController> T getNamedController(Class<T> clazz) {
		return getNamedController(clazz, FacesContext.getCurrentInstance());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends AbstractIdesController> T getNamedController(Class<T> clazz, FacesContext context) {
		String beanName = null;
		Named anno = clazz.getAnnotation(Named.class);

		if (anno == null) {
			throw new IllegalArgumentException("Class: " + clazz.getName() + " is not annotated with " + Named.class.getName());
		}
		beanName = anno.value();

		if (IdesUtils.isEmpty(beanName)) {
			String simpleName = clazz.getSimpleName();
			String firstChar = Character.toString(simpleName.charAt(0));
			beanName = clazz.getSimpleName().replaceFirst(firstChar, firstChar.toLowerCase());
		}

		ELContext elContext = context.getELContext();
		T bean = (T) context.getApplication().getELResolver().getValue(elContext, null, beanName);

		return bean;
	}

}
