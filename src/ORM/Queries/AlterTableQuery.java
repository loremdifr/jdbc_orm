package ORM.Queries;

import ORM.SQL.Columna;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;


//TODO:
public record AlterTableQuery(
	ArrayList<Columna> add_columns,
	ArrayList<Columna> modify_columns,
	ArrayList<Columna> drop_columns
) implements Query{

	@Override
	public PreparedStatement toPreparedStatement(Connection conexion) throws SQLException{
		//TODO:
		return null;
	}
}
