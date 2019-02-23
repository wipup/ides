package ports.soc.ides.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.controller.UserController;
import ports.soc.ides.interceptor.annotation.IdesRoleAllowed;
import ports.soc.ides.model.User;
import ports.soc.ides.model.constant.Role;
import ports.soc.ides.util.FacesUtils;
import ports.soc.ides.util.IdesUtils;

/**
 * This interceptor is just a second defense line
 * @author WIPU
 *
 */
@IdesRoleAllowed
@Interceptor
public class RoleEnforcer implements Serializable {

	private static final long serialVersionUID = -8637528625725038064L;

	private static final Logger log = LogManager.getRootLogger();

	@AroundInvoke
	public Object intercept(InvocationContext invocationCtx) throws Exception {
		Method m = invocationCtx.getMethod();

		FacesContext ctx = FacesContext.getCurrentInstance();
		if (ctx == null) {
			log.error("FacesContext is null. Halt invocation");
			return null;
		}

		FacesUtils.startLoggingSessionId();
		UserController userController = FacesUtils.getNamedController(UserController.class);
		if (userController == null) {
			log.error("No controller initialised. Halt invocation");
			return null;
		}

		Role[] allowedRoles = getDeclaredRoles(m);
		User u = userController.getUser();
		if (u == null) {
			log.error("User is null. Halt invocation");
			return null;
		}

		Set<Role> userRoles = u.getRoles();
		for (Role allowedRole : allowedRoles) {
			if (userRoles.contains(allowedRole)) {
				return invocationCtx.proceed();
			}
		}

		userController.addMessageFatal("Permission Denied", "");
		log.error("Permission denied: " + u.printDetail() + " isn't allowed to invoke method " + m.getName() + " of Roles" + IdesUtils.deepPrint(allowedRoles));
		return null;
	}

	private static Role[] getDeclaredRoles(Method m) {
		IdesRoleAllowed annotated = m.getAnnotation(IdesRoleAllowed.class);
		return annotated.value();
	}

}
