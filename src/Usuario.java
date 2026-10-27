import ORM.Annotations.ClavePrimaria;
import ORM.Annotations.GuardarComo;
import ORM.Annotations.NoSeGuarda;
import ORM.Relaciones.ManyToOne;
import ORM.SQL.TipoColumna;
import ORM.Clases.Tabla;

import java.time.LocalDateTime;

public class Usuario extends Tabla{

	@ClavePrimaria
	private Integer       id;
	private String        nombre;
	private int           edad;
	private LocalDateTime ultimaModificacion;

	@NoSeGuarda
	private ManyToOne<Empresa> empresa;

	@GuardarComo(tipo = TipoColumna.TEXT)
	private String biografia;

	public Usuario(){}

	public Usuario(
		String nombre,
		int    edad,
		String biografia
	){
		this.nombre    = nombre;
		this.edad      = edad;
		this.biografia = biografia;
		this.empresa   = new ManyToOne<>();

		//administrados por la DB:
		this.id                 = null;
		this.ultimaModificacion = null;
	}

	//getters
	public Integer       getId()                 { return id;                 }
	public String        getNombre()             { return nombre;             }
	public int           getEdad()               { return edad;               }
	public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
	public String        getBiografia()          { return biografia;          }
	public Empresa       getEmpresa()            { return empresa.get();      }


	@Override
	public String toString(){
		return "[id=%d, nombre=%s, edad=%d, bio=%s]".formatted(
			id, nombre, edad, biografia
		);
	}
}
