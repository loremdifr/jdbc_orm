package ORM.Annotations;

import ORM.SQL.TipoColumna;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GuardarComo{
	TipoColumna tipo() default TipoColumna.AUTO;
}
