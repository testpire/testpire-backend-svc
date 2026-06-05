package com.testpire.testpire.annotation;

import com.testpire.testpire.enums.Permission;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guards a controller method with one or more fine-grained {@link Permission}s.
 * Enforced by {@code AuthorizationAspect}; the caller's role-to-permission grants are loaded by
 * {@code PermissionService} from the {@code role_permissions} table.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    Permission[] value();
    boolean requireAll() default false; // if true, caller must hold ALL listed permissions; if false, ANY one suffices
}
