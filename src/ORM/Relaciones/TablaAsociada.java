package ORM.Relaciones;

import ORM.Clases.Tabla;

public interface TablaAsociada<T>{
	T get();
	boolean guardar();
}
