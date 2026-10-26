import ORM.Annotations.ClavePrimaria;
import ORM.Clases.Tabla;

public class Empresa extends Tabla{
	@ClavePrimaria
	final private Integer id;

	public Empresa(){
		this.id = null;
	}
}
