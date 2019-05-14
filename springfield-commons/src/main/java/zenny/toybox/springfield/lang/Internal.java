package zenny.toybox.springfield.lang;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Signifies that a public API (public class, annotation or package) is mainly
 * for internal use within the framework.
 *
 * @author Zenny Xu
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, ANNOTATION_TYPE, PACKAGE, TYPE_USE })
@Risk
public @interface Internal {
}
