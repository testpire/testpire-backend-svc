package com.testpire.testpire.enums;

public interface CDConstants {
	
	String STUDENT_ROLE = "hasRole('STUDENT')";
	String TEACHER_ROLE = "hasRole('TEACHER')";
	String ADMIN_ROLE = "hasRole('ADMIN')";
	String TEACHER_OR_ADMIN_ROLE = "hasRole('TEACHER') or hasRole('ADMIN')";
	String ANY_ROLE = "hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')";

}
