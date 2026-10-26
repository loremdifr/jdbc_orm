package ORM.Queries;
import java.util.ArrayList;

public sealed interface Where permits Where.BasicWhere, Where.NestedWhere{
	enum Conector{ AND, OR }
	Conector conector();
	String toString(int index);

	record BasicWhere(
		String     columna,
		Comparador comparador,
		String     valor,
		Conector   conector
	) implements Where{

		public BasicWhere(String columna, String comparador, String valor){
			this(columna, Comparador.fromString(comparador), valor, Conector.AND);
		}

		public BasicWhere(String columna, String comparador, String valor, Conector conector){
			this(columna, Comparador.fromString(comparador), valor, conector);
		}

		@Override
		public String toString(int index){
			final var str = new StringBuilder();
			if(index > 0) str.append(" ").append(conector);
			return str.append(columna)
					  .append(" ")
					  .append(comparador)
					  .append(" ")
					  .append(valor)
				   	  .toString();
		}
	}

	record NestedWhere(
		ArrayList<Where> wheres,
		Conector         conector
	) implements Where{
		@Override
		public String toString(int index){
			final var str = new StringBuilder();
			if(index > 0) str.append(" ").append(conector);
			str.append("(");
			for(int i = 0; i < wheres.size(); i++){
				str.append(wheres.get(i).toString(i));
			}
			str.append(")");
			return str.toString();
		}
	}
}
