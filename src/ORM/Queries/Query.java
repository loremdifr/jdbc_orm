package ORM.Queries;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

sealed public interface Query permits
	SelectQuery,
	AlterTableQuery,
	CreateTableQuery,
	InsertIntoQuery
{
	PreparedStatement toPreparedStatement(Connection conexion) throws SQLException;
}
