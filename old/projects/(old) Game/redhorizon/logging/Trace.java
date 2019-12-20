
// ===========================================
// Scanner's Java - Debugging level annotation
// ===========================================

package redhorizon.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface Trace {

	TraceLevel value() default TraceLevel.INFO;
}
