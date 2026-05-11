package me.croabeast.common;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * This annotation indicates that a String parameter, field, or method return value
 * should be a valid regular expression.
 *
 * <p> The annotation can be used to specify the syntax and flags of the regular expression,
 * as well as an example of a matching and a non-matching input.
 *
 * @see java.util.regex.Pattern
 * @see Language
 */
@Target({ METHOD, FIELD, PARAMETER, LOCAL_VARIABLE, ANNOTATION_TYPE })
@Language("RegExp")
public @interface Regex {}
