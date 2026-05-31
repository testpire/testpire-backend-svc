package com.testpire.testpire.enums;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum UserRole {
	SUPER_ADMIN(Set.of("SUPER_ADMIN", "INST_ADMIN", "TEACHER", "STUDENT")),
	INST_ADMIN(Set.of("INST_ADMIN", "TEACHER", "STUDENT")),
	TEACHER(Set.of("TEACHER")),
	STUDENT(Set.of("STUDENT"));

	private final Set<String> authorities;

	UserRole(Set<String> authorities) {
		this.authorities = authorities;
	}

	public Set<String> getAuthorityNames() {
		return authorities;
	}

	/**
	 * Whether a caller with this role may view/enumerate users of {@code targetRole}.
	 * A caller may view its own tier and any lower-privileged tier, but never a higher one
	 * (e.g. a TEACHER must not be able to list INST_ADMINs). Relies on this enum being
	 * declared in descending-privilege order (SUPER_ADMIN → INST_ADMIN → TEACHER → STUDENT).
	 */
	public boolean canViewRole(UserRole targetRole) {
		return this.ordinal() <= targetRole.ordinal();
	}

	public boolean canCreateRole(UserRole targetRole) {
		return switch (this) {
			case SUPER_ADMIN -> true; // Can create all roles
			case INST_ADMIN -> targetRole == TEACHER || targetRole == STUDENT;
			case STUDENT -> targetRole == STUDENT; // Students can only create student accounts (for themselves)
			case TEACHER -> targetRole == STUDENT; // Teachers cannot create other users
		};
	}
}