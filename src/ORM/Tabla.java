package ORM;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class Tabla implements ITabla{

//	@ClavePrimaria
//	private long id;
//	private LocalDateTime fechaModificacion;
//	private boolean borrado;

	@NoSeGuarda
	private boolean guardado;

	//relaciones..?

	public boolean guardar(){
		//TODO:
		return false;
	}

//	public long getId(){ return id; }
//	public LocalDateTime getFechaModificacion(){ return fechaModificacion; }
//	public boolean isGuardado(){ return guardado; }
//	public boolean isBorrado(){ return borrado; }

	public ArrayList<Tabla> buscar(){
		return SQL.buscar(getClass());
	}

	public Tabla buscarPorId(long id){
		return SQL.buscarPorId(getClass(), id);
	}


}
