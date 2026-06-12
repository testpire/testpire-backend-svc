package com.testpire.testpire.enums;

/**
 * Fine-grained permission catalog.
 *
 * <p>This enum is the compile-time catalog that endpoints reference via
 * {@code @RequirePermission}. Which roles actually hold each permission is NOT defined here — it is
 * stored in the {@code role_permissions} table (seeded by Flyway {@code V13}) and loaded into
 * {@code PermissionService}. Edit the DB and call {@code POST /api/rbac/reload} to change grants
 * without a redeploy.
 *
 * <p>Naming convention is {@code RESOURCE_ACTION}. Where the same resource/action historically had
 * a different role floor on different endpoints (e.g. institute-scoped teacher listing vs. the
 * teacher controller's own reads), distinct permissions are kept so behavior matches the previous
 * {@code @RequireRole} hierarchy exactly.
 */
public enum Permission {

    // --- Auth / self-service (all authenticated users) ---
    AUTH_LOGOUT("Log out the current session"),
    AUTH_PROFILE("View own authenticated profile"),

    // --- Institute ---
    INSTITUTE_CREATE("Create an institute"),
    INSTITUTE_UPDATE("Update an institute"),
    INSTITUTE_DELETE("Delete an institute"),
    INSTITUTE_READ("View an institute by id"),
    INSTITUTE_LIST("List all institutes"),
    INSTITUTE_SEARCH("Basic institute search"),
    INSTITUTE_SEARCH_ADVANCED("Advanced institute search"),
    INSTITUTE_TEACHER_LIST("List teachers within an institute"),
    INSTITUTE_STUDENT_LIST("List students within an institute"),

    // --- Course ---
    COURSE_CREATE("Create a course"),
    COURSE_UPDATE("Update a course"),
    COURSE_DELETE("Delete a course"),
    COURSE_READ("View/search courses"),

    // --- Batch ---
    BATCH_CREATE("Create a batch"),
    BATCH_UPDATE("Update a batch"),
    BATCH_DELETE("Delete a batch"),
    BATCH_READ("View/search batches"),

    // --- Subject ---
    SUBJECT_CREATE("Create a subject"),
    SUBJECT_UPDATE("Update a subject"),
    SUBJECT_DELETE("Delete a subject"),
    SUBJECT_READ("View/search subjects"),

    // --- Chapter ---
    CHAPTER_CREATE("Create a chapter"),
    CHAPTER_UPDATE("Update a chapter"),
    CHAPTER_DELETE("Delete a chapter"),
    CHAPTER_READ("View/search chapters"),

    // --- Topic ---
    TOPIC_CREATE("Create a topic"),
    TOPIC_UPDATE("Update a topic"),
    TOPIC_DELETE("Delete a topic"),
    TOPIC_READ("View/search topics"),

    // --- Topic material (teaching resources: ppt/pdf/video/note/link) ---
    TOPIC_MATERIAL_CREATE("Add a teaching material to a topic"),
    TOPIC_MATERIAL_UPDATE("Update a topic's teaching material"),
    TOPIC_MATERIAL_DELETE("Delete a topic's teaching material"),
    TOPIC_MATERIAL_READ("View/download a topic's teaching materials"),

    // --- Question ---
    QUESTION_CREATE("Create a question"),
    QUESTION_UPDATE("Update a question"),
    QUESTION_DELETE("Delete a question"),
    QUESTION_READ("View/search questions"),
    QUESTION_BULK_UPLOAD("Bulk upload questions from CSV"),
    CURRICULUM_BULK_UPLOAD("Bulk upload subjects/chapters/topics from CSV"),
    QUESTION_IMAGE_UPLOAD("Upload a question/option image"),

    // --- Student ---
    STUDENT_CREATE("Create a student"),
    STUDENT_UPDATE("Update a student"),
    STUDENT_DELETE("Delete a student"),
    STUDENT_READ("View a student by id"),
    STUDENT_LIST("List students"),
    STUDENT_SEARCH("Advanced student search"),
    STUDENT_DEBUG("Student debug endpoint"),
    STUDENT_PROFILE_READ("View student self profile"),
    STUDENT_PROFILE_UPDATE("Update student self profile"),
    STUDENT_PEERS_READ("View student peers"),

    // --- Lead / enquiry pipeline ---
    LEAD_CREATE("Create a lead/enquiry"),
    LEAD_READ("View a lead by id"),
    LEAD_LIST("List leads"),
    LEAD_SEARCH("Advanced lead search"),
    LEAD_UPDATE("Update a lead / follow-up"),
    LEAD_DELETE("Delete a lead"),
    LEAD_CONVERT("Convert a lead into an enrolled student"),

    // --- Teacher ---
    TEACHER_CREATE("Create a teacher"),
    TEACHER_UPDATE("Update a teacher"),
    TEACHER_DELETE("Delete a teacher"),
    TEACHER_READ("View a teacher by id"),
    TEACHER_LIST("List teachers"),
    TEACHER_SEARCH("Advanced teacher search"),
    TEACHER_DEBUG("Teacher debug endpoint"),
    TEACHER_PROFILE_READ("View teacher self profile"),
    TEACHER_PROFILE_UPDATE("Update teacher self profile"),

    // --- Test / exam engine ---
    TEST_CREATE("Create a test"),
    TEST_UPDATE("Update a test / manage its questions"),
    TEST_DELETE("Delete a test"),
    TEST_READ("View/list tests (staff)"),
    TEST_PUBLISH("Publish a test"),
    TEST_ASSIGN("Assign a test to a course/batch/student"),
    TEST_RESULTS_READ("View every student's marks for a test"),
    TEST_TAKE("Take a test (start/answer/submit)"),
    TEST_ATTEMPT_READ("View own test attempts/results"),

    // --- Generic user management ---
    USER_CREATE("Register a user"),
    USER_READ("View/search users by role"),
    USER_UPDATE("Update a user"),
    USER_DELETE("Delete a user"),
    USER_RESEND_INVITATION("Resend a user invitation"),

    // --- System / super-admin ---
    SYSTEM_USERS_READ("View any user across the system"),
    SYSTEM_DASHBOARD("View the system dashboard"),
    RBAC_MANAGE("View and reload role-permission mappings");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
