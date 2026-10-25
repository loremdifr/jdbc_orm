package ORM.Exceptions;

import ORM.Tabla;

public class TablaNoRegistradaException extends ORMException{
	public TablaNoRegistradaException(Class<? extends Tabla> tabla){
		super("La Tabla %s no fue registrada en SQL con SQL.registrarTabla() aún.".formatted(
			tabla
		));
	}
}
