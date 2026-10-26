package ORM.Clases;

import ORM.Annotations.NoSeGuarda;
import ORM.DB;
import ORM.Exceptions.ORMException;
import java.sql.SQLException;


public abstract class Tabla{

	@NoSeGuarda
	private boolean guardado = false;

	//constructor vacio para que DB.resultSetAFilas pueda instanciar
	public Tabla(){}

	//TODO: relaciones..?

	public boolean guardado(){ return guardado; }
	public boolean guardar() throws SQLException{
		final var result = DB.guardar(this);
		if(result) guardado = true;
		return result;
	}

	public Object PK() throws ORMException{
		return DB.valorDePK(this);
	}

}
