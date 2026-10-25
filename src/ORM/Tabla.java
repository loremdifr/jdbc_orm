package ORM;

import ORM.Annotations.NoSeGuarda;
import ORM.Exceptions.TablaModificadaException;
import ORM.Exceptions.TablaNoRegistradaException;
import ORM.Interfaces.ITabla;

public abstract class Tabla implements ITabla{

	@NoSeGuarda
	private boolean guardado = false;

	//TODO: relaciones..?

	public boolean guardar(){ return SQL.guardar(this); }
	public boolean guardado(){ return guardado; }

	public Object PK()
	throws TablaModificadaException,
		TablaNoRegistradaException
	{
		return SQL.valorDePK(this);
	}

}
