import ORM.Annotations.ClavePrimaria;
import ORM.Annotations.GuardarComo;
import ORM.Relaciones.ManyToOne;
import ORM.SQL.TipoColumna;
import ORM.Clases.Tabla;

import java.time.LocalDateTime;

public class Usuario extends Tabla{

	@ClavePrimaria
	private final Integer            id;
	private final String             nombre;
	private final int                edad;
	private final LocalDateTime      ultimaModificacion;
	private final ManyToOne<Empresa> empresa;

	@GuardarComo(tipo = TipoColumna.TEXT)
	private final String biografia;

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
}
