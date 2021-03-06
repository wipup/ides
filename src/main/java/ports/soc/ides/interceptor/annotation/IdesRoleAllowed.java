package ports.soc.ides.interceptor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import ports.soc.ides.model.constant.Role;

@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface IdesRoleAllowed {
	
	@Nonbinding
	Role[] value() default {}; 
	
	@Nonbinding
	String description() default "";
}
