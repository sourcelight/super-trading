package com.rick.supertrading.domain.model;

/**
 * Authorization role of a console {@link AppUser}.
 *
 * <ul>
 *   <li>{@link #USER} — sees and manages only data they own.</li>
 *   <li>{@link #ADMIN} — bypasses ownership filters; sees everything.</li>
 * </ul>
 */
public enum Role {
    USER,
    ADMIN
}
