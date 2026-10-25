package ORM.Relaciones;

import ORM.Clases.Tabla;

public class ManyToOne<T extends Tabla> implements TablaAsociada<T>{

	final private T tabla;

	public ManyToOne(){
		this.tabla = null;
	}

	public T get(){ return tabla; }

	public boolean guardar(){ return false; } //TODO:
}
