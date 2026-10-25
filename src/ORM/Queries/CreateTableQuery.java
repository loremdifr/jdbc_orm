package ORM.Queries;

import ORM.SQL.Schema;

import java.util.ArrayList;

public record CreateTableQuery(Schema schema) implements Query{

	//TODO: split function

	@Override
	public String toString(){
		final var cols_str = new ArrayList<String>();

		for(final var col : schema.columnas().values()){
			//column_name type AUTO_INCREMENT NOT NULL UNIQUE DEFAULT X
			final var col_str = new ArrayList<String>();

			col_str.add(col.nombre()); //nombre
			col_str.add(col.tipo().sqlString()); //tipo

			//TODO: fk?
			if(col.pk()){
				col_str.add("AUTO_INCREMENT");
				col_str.add("NOT NULL");
				col_str.add("UNIQUE");
			}
//				col_str.add("DEFAULT %s");

			cols_str.add(String.join(" ", col_str));
		}

		cols_str.add("PRIMARY KEY (%s)".formatted(
			schema.columnas().get(schema.pk()).nombre()
		));

		//TODO: fkeys?

		return "CREATE TABLE IF NOT EXISTS %s (%s);".formatted(
			schema.tabla(), String.join(",\n", cols_str));
	}

}
