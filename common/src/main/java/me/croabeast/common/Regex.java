package me.croabeast.common;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Indicates that a {@link String} parameter, field, or method return value should be a valid regular expression.
 * <p>
 * This annotation can be used to document the syntax and flags of the regular expression,
 * as well as examples of matching and non-matching inputs.
 * </p>
 *
 * @see java.util.regex.Pattern
 * @see Language
 */
@Target({ METHOD, FIELD, PARAMETER, LOCAL_VARIABLE, ANNOTATION_TYPE })
@Language("RegExp")
public @interface Regex {}
