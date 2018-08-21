package ports.soc.ides.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.interceptor.annotation.LogPerformance;
import ports.soc.util.FacesUtils;
import ports.soc.util.IdesUtils;

@LogPerformance
@Interceptor
public class ActionLogger implements Serializable {

	private static final long serialVersionUID = -8637528625725038064L;

	private static final Logger log = LogManager.getRootLogger();

	/**
	 * Log method that is invoked by the servlet
	 */
	@AroundInvoke
	public Object intercept(InvocationContext invocationContext) throws Exception{
		long start = System.currentTimeMillis();
		Method method = invocationContext.getMethod();
		String className = method.getDeclaringClass().getSimpleName();
		
		LogPerformance annotation = method.getAnnotation(LogPerformance.class);
		String description = annotation.note();
		String level = annotation.level().toUpperCase();
		boolean logParameters = annotation.logParameters();
		
		FacesUtils.startLoggingSessionId();
		try {
			return invocationContext.proceed();
		} finally {
			FacesUtils.startLoggingSessionId();
			
			long time = System.currentTimeMillis() - start;
			StringBuilder sb = new StringBuilder();
			sb.append("method=").append(method.getName())
			.append(", processingTime=").append(time).append("ms");
			if (logParameters) {
				sb.append(", parameter=").append(IdesUtils.deepPrint(invocationContext.getParameters()));
			}
			sb.append(", class=").append(className)
			.append(", note=\"").append(description)
			.append("\"");
			
			Level l = Level.getLevel(level);
			if (l == null) {
				l = Level.INFO;
			}
			log.log(l, sb.toString());
		}

	}

}
