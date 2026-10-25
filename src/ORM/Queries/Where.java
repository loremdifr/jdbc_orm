package ORM.Queries;
import java.util.ArrayList;

public sealed interface Where permits Where.BasicWhere, Where.NestedWhere{
	enum Conector{ AND, OR }
	Conector conector();

	record BasicWhere(
		String     columna,
		Comparador comparador,
		String     valor,
		Conector   conector
	) implements Where{

		public BasicWhere(String columna, String comparador, String valor){
			this(columna, Comparador.fromString(comparador), valor, Conector.AND);
		}

	}

	record NestedWhere(
		ArrayList<Where> wheres,
		Conector         conector
	) implements Where{}
}
