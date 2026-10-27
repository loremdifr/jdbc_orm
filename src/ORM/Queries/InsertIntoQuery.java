package ORM.Queries;

import ORM.Clases.Tabla;
import ORM.DB;
import ORM.Exceptions.ORMException;
import ORM.SQL.Schema;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

public record InsertIntoQuery(
	Schema schema,
	Tabla fila
) implements Query{

	@Override
	public PreparedStatement toPreparedStatement(Connection conexion) throws SQLException{
		//TODO: only update dirty fields

		final var query_str =
		"""
		INSERT INTO %s (%s) VALUES (%s) AS %s
		ON DUPLICATE KEY UPDATE %s;
		""";

		final var alias_fila = "NUEVA_FILA";
		final var columnas   = new ArrayList<String>();
		final var valores    = new ArrayList<>();
		final var update     = new ArrayList<String>();
		try{
			for(final var col : schema.columnas().values()){
				final var valor = DB.valorDeColumna(fila, col);

				// salteamos la PK si es null, porque estamos guardando
				// algo nuevo y queremos que la DB la inserte
				if(col.pk() && valor == null) continue;

				columnas.add(col.nombre());
				valores.add(valor);
				update.add("%s = %s.%s".formatted(
					 col.nombre(), alias_fila, col.nombre()
				));
			}
		}catch(ORMException e){
			throw new RuntimeException(e);
		}

		final var statement = conexion.prepareStatement(
			query_str.formatted(
				schema.tabla(),
				String.join(", ", columnas),
				"?, ".repeat(valores.size() - 1) + " ?",
				alias_fila,
				String.join(", ", update)
			),
			Statement.RETURN_GENERATED_KEYS //necesario para poder hidratar la PK
		);


		for(int i = 0; i < valores.size(); i++){
			final var valor = valores.get(i);
			final var idx   = i + 1;
			//TODO: esto deberia estar centralizado con la operacion inversa
			switch(valor){
				case Integer in   -> statement.setInt       (idx, in);
				case String s     -> statement.setString    (idx, s);
				case BigDecimal b -> statement.setBigDecimal(idx, b);
				case Boolean b    -> statement.setBoolean   (idx, b);
				case Byte b       -> statement.setByte      (idx, b);
				case Date d       -> statement.setDate      (idx, d);
				case Time t       -> statement.setTime      (idx, t);
				case Timestamp t  -> statement.setTimestamp (idx, t);
				case Double d     -> statement.setDouble    (idx, d);
				case Float f      -> statement.setFloat     (idx, f);
				case Long l       -> statement.setLong      (idx, l);
				case null         -> statement.setNull(idx, Types.NULL);
				default -> throw new UnsupportedOperationException();
			}
		}
		return statement;
	}
}
