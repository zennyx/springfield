package zenny.toybox.springfield.lang;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Signifies that a public API (public class, method or field) is subject to
 * incompatible changes, or even removal, in a future release. An API bearing
 * this annotation is exempt from any compatibility guarantees made by its
 * containing library. Note that the presence of this annotation implies nothing
 * about the quality or performance of the API in question, only the fact that
 * it is not "API-frozen."
 * <p>
 * It is generally safe for <em>applications</em> to depend on lab APIs, at the
 * cost of some extra work during upgrades. However it is generally inadvisable
 * for <em>libraries</em> (which get included on users' CLASSPATHs, outside the
 * library developers' control) to do so.
 *
 * @author Zenny Xu
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
@Risk
public @interface Lab {
}
