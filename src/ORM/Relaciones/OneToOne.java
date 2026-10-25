package ORM.Relaciones;

import ORM.Clases.Tabla;

public class OneToOne<T extends Tabla> implements TablaAsociada<T>{

	final private T tabla;

	public OneToOne(){
		this.tabla = null;
	}

	public T get(){ return tabla; }

	public boolean guardar(){ return false; } //TODO:
}
