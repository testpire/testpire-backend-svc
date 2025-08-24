package com.testpire.testpire.constants;

/**
 * Application-wide constants for production-ready code
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
        // Prevent instantiation
    }

    // HTTP Headers
    public static final class Headers {
        public static final String AUTHORIZATION = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        
        private Headers() {}
    }

    // HTTP Status Messages
    public static final class Messages {
        // Success Messages
        public static final String LOGIN_SUCCESS = "Login successful";
        public static final String LOGOUT_SUCCESS = "Logout successful";
        public static final String REGISTRATION_SUCCESS = "Registration successful";
        public static final String STUDENT_REGISTRATION_SUCCESS = "Student registered successfully";
        public static final String INSTITUTE_CREATED_SUCCESS = "Institute created successfully";
        public static final String INSTITUTE_UPDATED_SUCCESS = "Institute updated successfully";
        public static final String INSTITUTE_DEACTIVATED_SUCCESS = "Institute deactivated successfully";
        public static final String USER_CREATED_SUCCESS = "User created successfully";
        public static final String USER_UPDATED_SUCCESS = "User updated successfully";
        public static final String USER_DELETED_SUCCESS = "User deleted successfully";
        public static final String DASHBOARD_RETRIEVED_SUCCESS = "Dashboard retrieved successfully";
        public static final String PROFILE_RETRIEVED_SUCCESS = "Profile retrieved successfully";
        public static final String SYSTEM_DASHBOARD_RETRIEVED_SUCCESS = "System dashboard retrieved successfully";
        public static final String TEACHER_DASHBOARD_RETRIEVED_SUCCESS = "Teacher dashboard retrieved successfully";
        public static final String PEERS_RETRIEVED_SUCCESS = "Peers retrieved successfully";
        public static final String TEACHERS_RETRIEVED_SUCCESS = "Teachers retrieved successfully";
        public static final String INSTITUTE_INFO_RETRIEVED_SUCCESS = "Institute information retrieved successfully";

        // Error Messages
        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String LOGOUT_FAILED = "Logout failed";
        public static final String REGISTRATION_FAILED = "Registration failed";
        public static final String INSTITUTE_NOT_FOUND = "Institute not found with ID: ";
        public static final String INSTITUTE_ALREADY_EXISTS = "Institute with code %s already exists";
        public static final String INSTITUTE_EMAIL_EXISTS = "Institute with email %s already exists";
        public static final String USER_ALREADY_EXISTS = "User with username %s already exists";
        public static final String USER_EMAIL_EXISTS = "User with email %s already exists";
        public static final String USER_NOT_FOUND = "User not found with ID: ";
        public static final String INSUFFICIENT_PERMISSIONS = "Insufficient permissions to create this user type";
        public static final String ACCESS_DENIED = "Access denied";
        public static final String ACCESS_DENIED_INSTITUTE = "Access denied - user not in your institute";
        public static final String ACCESS_DENIED_STUDENT_ONLY = "Access denied - only students can access this endpoint";
        public static final String USER_NOT_ROLE = "User is not a %s";
        public static final String FAILED_TO_FETCH = "Failed to fetch";
        public static final String FAILED_TO_CREATE = "Failed to create";
        public static final String FAILED_TO_UPDATE = "Failed to update";
        public static final String FAILED_TO_DELETE = "Failed to delete";
        public static final String FAILED_TO_SEARCH = "Failed to search";
        public static final String INTERNAL_SERVER_ERROR = "Internal server error";
        public static final String INSUFFICIENT_PERMISSIONS_MESSAGE = "Insufficient permissions";

        // Validation Messages
        public static final String INSTITUTE_CODE_REQUIRED = "Institute code is required";
        public static final String INSTITUTE_NAME_REQUIRED = "Institute name is required";
        public static final String ADDRESS_REQUIRED = "Address is required";
        public static final String CITY_REQUIRED = "City is required";
        public static final String STATE_REQUIRED = "State is required";
        public static final String COUNTRY_REQUIRED = "Country is required";
        public static final String POSTAL_CODE_REQUIRED = "Postal code is required";
        public static final String PHONE_REQUIRED = "Phone number is required";
        public static final String EMAIL_REQUIRED = "Email is required";
        public static final String USERNAME_REQUIRED = "Username is required";
        public static final String FIRST_NAME_REQUIRED = "First name is required";
        public static final String LAST_NAME_REQUIRED = "Last name is required";
        public static final String PASSWORD_REQUIRED = "Password is required";
        public static final String USER_ROLE_REQUIRED = "User role is required";
        public static final String INSTITUTE_ID_REQUIRED = "Institute ID is required";

        // Cognito Messages
        public static final String COGNITO_SIGNUP_FAILED = "Cognito signup failed";
        public static final String COGNITO_CONFIRM_FAILED = "Cognito confirmation failed";
        public static final String COGNITO_LOGIN_FAILED = "Cognito login failed";
        public static final String COGNITO_LOGOUT_FAILED = "Cognito logout failed";

        private Messages() {}
    }

    // Cognito Attribute Names
    public static final class CognitoAttributes {
        public static final String EMAIL = "email";
        public static final String GIVEN_NAME = "given_name";
        public static final String FAMILY_NAME = "family_name";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String CUSTOM_ROLE = "custom:role";
        public static final String CUSTOM_INSTITUTE_ID = "custom:instituteId";
        public static final String SECRET_HASH = "SECRET_HASH";

        private CognitoAttributes() {}
    }

    // Database Constants
    public static final class Database {
        public static final String USERS_TABLE = "users";
        public static final String INSTITUTES_TABLE = "institutes";
        public static final String CREATED_AT_COLUMN = "created_at";
        public static final String UPDATED_AT_COLUMN = "updated_at";
        public static final String CREATED_BY_COLUMN = "created_by";
        public static final String INSTITUTE_ID_COLUMN = "institute_id";
        public static final String COGNITO_USER_ID_COLUMN = "cognito_user_id";

        private Database() {}
    }

    // Validation Constraints
    public static final class Validation {
        public static final int USERNAME_MIN_LENGTH = 3;
        public static final int USERNAME_MAX_LENGTH = 50;
        public static final int PASSWORD_MIN_LENGTH = 8;
        public static final int FIRST_NAME_MAX_LENGTH = 50;
        public static final int LAST_NAME_MAX_LENGTH = 50;
        public static final int INSTITUTE_NAME_MIN_LENGTH = 2;
        public static final int INSTITUTE_NAME_MAX_LENGTH = 100;
        public static final int INSTITUTE_CODE_MIN_LENGTH = 2;
        public static final int INSTITUTE_CODE_MAX_LENGTH = 20;
        public static final int ADDRESS_MAX_LENGTH = 500;
        public static final int CITY_MAX_LENGTH = 100;
        public static final int STATE_MAX_LENGTH = 100;
        public static final int COUNTRY_MAX_LENGTH = 100;
        public static final int POSTAL_CODE_MAX_LENGTH = 20;
        public static final int PHONE_MAX_LENGTH = 20;
        public static final int DESCRIPTION_MAX_LENGTH = 1000;

        private Validation() {}
    }

    // API Endpoints
    public static final class Endpoints {
        public static final String API_BASE = "/api";
        public static final String AUTH_BASE = API_BASE + "/auth";
        public static final String INSTITUTE_BASE = API_BASE + "/institute";
        public static final String SUPER_ADMIN_BASE = API_BASE + "/super-admin";
        public static final String USER_MANAGEMENT_BASE = API_BASE + "/users";
        public static final String TEACHER_BASE = API_BASE + "/teacher";
        public static final String STUDENT_BASE = API_BASE + "/student";
        public static final String INST_ADMIN_BASE = API_BASE + "/inst-admin";

        // Auth endpoints
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
        public static final String REGISTER_STUDENT = "/register/student";
        public static final String PROFILE = "/profile";

        // Institute endpoints
        public static final String CREATE_INSTITUTE = "";
        public static final String UPDATE_INSTITUTE = "/{id}";
        public static final String DELETE_INSTITUTE = "/{id}";
        public static final String GET_INSTITUTE = "/{id}";
        public static final String GET_INSTITUTE_BY_CODE = "/code/{code}";
        public static final String GET_ALL_INSTITUTES = "";
        public static final String SEARCH_INSTITUTES = "/search";

        // User management endpoints
        public static final String REGISTER_USER = "/register";
        public static final String GET_USERS_BY_ROLE = "/{role}";
        public static final String SEARCH_USERS = "/{role}/search";
        public static final String GET_USER_BY_ID = "/{role}/{id}";
        public static final String UPDATE_USER = "/{role}/{id}";
        public static final String DELETE_USER = "/{role}/{id}";

        // Dashboard endpoints
        public static final String DASHBOARD = "/dashboard";
        public static final String SYSTEM_DASHBOARD = "/dashboard";
        public static final String TEACHER_DASHBOARD = "/dashboard";

        // Student endpoints
        public static final String PEERS = "/peers";
        public static final String TEACHERS = "/teachers";
        public static final String INSTITUTE_INFO = "/institute";

        private Endpoints() {}
    }

    // Security Constants
    public static final class Security {
        public static final String ROLE_PREFIX = "ROLE_";
        public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
        public static final String INST_ADMIN_ROLE = "INST_ADMIN";
        public static final String TEACHER_ROLE = "TEACHER";
        public static final String STUDENT_ROLE = "STUDENT";

        private Security() {}
    }

    // Audit Constants
    public static final class Audit {
        public static final String SELF_REGISTRATION = "self-registration";
        public static final String SYSTEM = "system";

        private Audit() {}
    }

    // Phone Number Constants
    public static final class Phone {
        public static final String DEFAULT_PHONE = "+919905536608";

        private Phone() {}
    }
} 