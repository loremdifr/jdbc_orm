package ORM.Clases;

import ORM.Annotations.NoSeGuarda;
import ORM.DB;
import ORM.Exceptions.TablaModificadaException;
import ORM.Exceptions.TablaNoRegistradaException;


public abstract class Tabla{

	@NoSeGuarda
	private boolean guardado = false;

	//TODO: relaciones..?

	public boolean guardar(){ return DB.guardar(this); }
	public boolean guardado(){ return guardado; }

	public Object PK()
	throws TablaModificadaException,
		TablaNoRegistradaException
	{
		return DB.valorDePK(this);
	}

}
