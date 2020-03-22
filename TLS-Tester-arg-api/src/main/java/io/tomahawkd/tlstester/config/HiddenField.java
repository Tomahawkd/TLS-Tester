package io.tomahawkd.tlstester.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hidden field for arg delegate to avoid unintended access.
 * For more information {@link AbstractArgDelegate#getField(String, Class)}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HiddenField {
}
