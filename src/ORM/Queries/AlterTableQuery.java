package ORM.Queries;

import ORM.SQL.Columna;
import java.util.ArrayList;


//TODO:
public record AlterTableQuery(
	ArrayList<Columna> add_columns,
	ArrayList<Columna> modify_columns,
	ArrayList<Columna> drop_columns
) implements Query{}
