package com.testpire.testpire.annotation;

import com.testpire.testpire.enums.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    UserRole[] value() default {};
    boolean requireAll() default false; // if true, user must have ALL roles, if false, user needs ANY role
}
