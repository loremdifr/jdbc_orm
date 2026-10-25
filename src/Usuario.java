import ORM.Annotations.ClavePrimaria;
import ORM.Annotations.Columna;
import ORM.SQL;
import ORM.Tabla;

import java.time.LocalDateTime;

public class Usuario extends Tabla{

	@ClavePrimaria
	private long id;
	private String nombre;
	private int edad;
	private LocalDateTime ultimaModificacion;

	@Columna(tipo = SQL.TipoColumna.TEXT)
	private String biografia;

	public Usuario(
		String nombre,
		int edad,
		String biografia
	){
		this.nombre = nombre;
		this.edad = edad;
		this.biografia = biografia;
		this.ultimaModificacion = null;
	}
}
