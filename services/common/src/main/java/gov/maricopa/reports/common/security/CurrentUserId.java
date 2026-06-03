package gov.maricopa.reports.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resolves to the authenticated user's id (extracted from the JWT). Endpoints that declare
 * a parameter with this annotation require a valid access token, returning 401 otherwise.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}
