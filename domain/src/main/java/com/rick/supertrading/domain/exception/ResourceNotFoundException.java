package com.rick.supertrading.domain.exception;

/**
 * Thrown when a requested entity does not exist <em>or</em> is not visible to the
 * current user. The two cases are deliberately indistinguishable to callers so we
 * don't leak the existence of other users' data.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entity, Object id) {
        super(entity + " not found: " + id);
    }
}
