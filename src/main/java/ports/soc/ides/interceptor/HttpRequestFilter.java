package ports.soc.ides.interceptor;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.controller.helper.IdesPage;
import ports.soc.ides.logger.LoggerConfigurationFactory;
import ports.soc.util.IdesUtils;

@WebFilter(servletNames = { "Faces Servlet" })
public class HttpRequestFilter implements Filter {

	private static final Logger log = LogManager.getRootLogger();
	
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpReq = null;
		HttpServletResponse res = null;
		if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
			httpReq = (HttpServletRequest) req;
			res = (HttpServletResponse) resp;
			
			if (!httpReq.isRequestedSessionIdValid()) {
				log.info("Invalid requested session: " + httpReq.getRequestedSessionId());
				res.sendRedirect(httpReq.getContextPath());
				return;
			}
			
			if (log.getLevel().isLessSpecificThan(LoggerConfigurationFactory.HUNT)) {
				log.log(LoggerConfigurationFactory.HUNT, createHttpLogMessage(httpReq));
			}
			
			if (!isAllowed(httpReq) && (resp instanceof HttpServletResponse)) {
				String uri = httpReq.getRequestURI();
				log.error("invalid requested uri=" + uri + ". Redirect to " + httpReq.getContextPath());
				res.sendRedirect(httpReq.getContextPath());
				return;
			}
		}

		try {
			chain.doFilter(req, resp);
		} catch (Exception e) {
			String sessionId = "null";
			String params = "null";

			if (httpReq != null) {
				HttpSession session = httpReq.getSession(false);
				if (session != null) {
					sessionId = session.getId();
				}
				params = IdesUtils.deepPrint(httpReq.getParameterMap());
			}

			log.error("Error on HTTP Request[sessionId=" + sessionId + ", parameter=" + params + "]", e);
			throw e;
		}
	}
	
	private boolean isAllowed(HttpServletRequest httpReq) {
		String uri = httpReq.getRequestURI();
		String ctxPath = httpReq.getContextPath();
		
		if (uri.equals(ctxPath) || uri.equals(ctxPath  + "/")) {
			// if URI is /ides/
			return true;
		}
		
		if (uri.equals(ctxPath + IdesPage.AGREEMENT.getPath())) {
			// if URI is /ides/view/agreement.xhtml
			return true;
		}
		
		if (uri.equals(ctxPath + IdesPage.VIEW_IDEA.getPath())) {
			// if URI is /ides/view/idea.xhtml
			return true;
		}
		
		
		boolean ajax = "XMLHttpRequest".equals(httpReq.getHeader("X-Requested-With"));
		String indexUri = ctxPath + IdesPage.INDEX.getPath();
		
		if (ajax && uri.equals(indexUri)) {
			// if ajax and URI is /ides/view/index.xhtml
			return true;
		}
		
		String errorUri = ctxPath + IdesPage.ERROR.getPath();
		if (uri.equals(errorUri)) {
			//if ajax and URI is /ides/view/error.xhtml
			return false;
		}
		
		if (uri.startsWith(ctxPath + "/view") && !httpReq.getMethod().equals("POST")) {
			//if request /ides/view* and not post, reject 
			//all ajax requests are POST
			return false;
		}
		
		return true;
	}
	
	private String createHttpLogMessage(HttpServletRequest httpReq) {
		String sessionId = httpReq.getRequestedSessionId();
		
		StringBuilder sb = new StringBuilder();

		HttpSession session = httpReq.getSession(false);
		 
		ZonedDateTime createDt = null;
		ZonedDateTime lastDt = null;
		if (session != null) {
			long createTime = session.getCreationTime();
			long lastTime = session.getLastAccessedTime();
			
			lastDt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastTime), ZoneOffset.UTC);
			createDt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(createTime), ZoneOffset.UTC);
		}

		sb.append("Http headers: requestedSessionId=").append(sessionId)
		//.append(", sessionId=").append(id)
		.append(", last access=").append(lastDt)
		.append(", creation time=").append(createDt)
		.append(", method=").append(httpReq.getMethod())
		.append(", IP Address=").append(httpReq.getRemoteAddr())
		.append(", host=").append(httpReq.getRemoteHost())
		.append(", url=").append(httpReq.getRequestURL());

		sb.append(", headers={");
		Enumeration<String> headerNames = httpReq.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			String value = httpReq.getHeader(name);
			sb.append(name).append("=").append(value).append(", ");
		}
		sb.append("}");
		
		return sb.toString();
	}
	

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

}
