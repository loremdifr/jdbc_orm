package ORM.Annotations;

import ORM.SQL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Columna{
	SQL.TipoColumna tipo() default SQL.TipoColumna.AUTO;
}
