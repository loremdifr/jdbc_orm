package ORM.Relaciones;

import ORM.Clases.Tabla;

import java.util.ArrayList;

public class OneToMany<T extends Tabla> implements TablaAsociada<ArrayList<T>>{

	final private ArrayList<T> tabla;

	public OneToMany(){
		this.tabla = new ArrayList<>();
	}

	public ArrayList<T> get(){ return tabla; }

	public boolean guardar(){ return false; } //TODO:
}
