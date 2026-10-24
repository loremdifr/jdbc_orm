import ORM.Columna;
import ORM.ITabla;
import ORM.SQL;

public class Usuario implements ITabla{

	private long id;
	private boolean borrado;
	private String nombre;
	private int edad;

	@Columna(tipo = SQL.TipoColumna.TEXT)
	private String biografia;

	@Override
	public long getId(){
		return 0;
	}

	@Override
	public boolean getFechaModificacion(){
		return false;
	}

	@Override
	public boolean getBorrado(){
		return false;
	}

	@Override
	public boolean guardar(){
		return false;
	}

	@Override
	public boolean estaGuardado(){
		return false;
	}
}
