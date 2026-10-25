package ORM.Queries;
import java.util.ArrayList;

public final class SelectQuery implements Query{

	final private ArrayList<String> select;
	final private String            from;
	final private ArrayList<Where>  wheres;
	final private ArrayList<String> groupBy;
	final private ArrayList<String> orderBy;
	final private ArrayList<String> limit;

	public SelectQuery(String from){
		this.from    = from;
		this.select  = new ArrayList<>();
		this.wheres  = new ArrayList<>();
		this.groupBy = new ArrayList<>();
		this.orderBy = new ArrayList<>();
		this.limit   = new ArrayList<>();
	}

	//where
	public SelectQuery where(String columna, String valor){
		//TODO: eq vs is vs in vs like?
		return where(columna, "=", valor);
	}
	public SelectQuery where(
		String columna,
		String comparador,
		String valor
	){
		//TODO: validar campo

		wheres.add(new Where.BasicWhere(columna, comparador, valor));
		return this;
	}

	public SelectQuery andWhere(
		String columna,
		String comparador,
		String valor
	){
		//TODO:
		return this;
	}
	public SelectQuery orWhere(
		String columna,
		String comparador,
		String valor
	){
		//TODO:
		return this;
	}

	//
	public SelectQuery groupBy(){ return this; }
	public SelectQuery limit(){ return this; }
	public SelectQuery orderBy(){ return this; }

	//results
	public void first(){ return; }
	public void toList(){ return; }
	public void count(){ return; }
	public void avg(){ return; }
	public void min(){ return; }
	public void max(){ return; }
	public void sum(){ return; }
//	public void distinct(){ return; }



}
