package ORM.SQL;

import ORM.Exceptions.ORMException;
import ORM.Exceptions.TipoDeColumnaInesperadoException;
import ORM.Exceptions.TipoDeColumnaInvalidaException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;

public enum TipoColumna{
	AUTO    (""),
	BOOL    ("BOOL"),
	INT     ("INT"),
	BIGINT  ("BIGINT"),
	FLOAT   ("FLOAT"),
	DOUBLE  ("DOUBLE"),
	DECIMAL ("DECIMAL"),
	VARCHAR ("VARCHAR", 255), //TODO: parametrizar?
	TEXT    ("TEXT"),
	DATETIME("DATETIME"),
	;

	final private String sqlString;
	final private Integer column_size;

	TipoColumna(String sqlString, Integer column_size){
		this.sqlString 	 = sqlString;
		this.column_size = column_size;
	}

	TipoColumna(String sqlString){ this(sqlString, null); }

	@Override
	public String toString(){ return sqlString; }

	public String toStringWithSize(){
		if(column_size == null) return toString();
		return "%s(%d)".formatted(sqlString, column_size);
	}

	static public TipoColumna fromString(String str)
	throws TipoDeColumnaInesperadoException{
		for(var tipo_columna : values()){
			if(tipo_columna.toString().equals(str)) return tipo_columna;
		}
		throw new TipoDeColumnaInesperadoException(str);
	}

	static public TipoColumna fromSqlType(int sql_type)
	throws TipoDeColumnaInesperadoException{
		return switch(sql_type){
			case Types.BOOLEAN     -> BOOL;
			case Types.INTEGER     -> INT;
			case Types.BIGINT      -> BIGINT;
			case Types.FLOAT       -> FLOAT;
			case Types.DOUBLE      -> DOUBLE;
			case Types.DECIMAL     -> DECIMAL;
			case Types.VARCHAR     -> VARCHAR;
			case Types.LONGVARCHAR -> TEXT;
			case Types.TIMESTAMP   -> DATETIME;
			default -> throw new TipoDeColumnaInesperadoException("" + sql_type);
		};
	}

	static public TipoColumna fromField(Field field) throws TipoDeColumnaInvalidaException{
		final var tipo = field.getType();
		if(tipo == Boolean.class)       return TipoColumna.BOOL;
		if(tipo == boolean.class)       return TipoColumna.BOOL;
		if(tipo == int.class)           return TipoColumna.INT;
		if(tipo == Integer.class)       return TipoColumna.INT;
		if(tipo == long.class)          return TipoColumna.BIGINT;
		if(tipo == Long.class)          return TipoColumna.BIGINT;
		if(tipo == float.class)         return TipoColumna.FLOAT;
		if(tipo == Float.class)         return TipoColumna.FLOAT;
		if(tipo == double.class)        return TipoColumna.DOUBLE;
		if(tipo == Double.class)        return TipoColumna.DOUBLE;
		if(tipo == BigDecimal.class)    return TipoColumna.DECIMAL;
		if(tipo == String.class)        return TipoColumna.VARCHAR;
		if(tipo == LocalDateTime.class) return TipoColumna.DATETIME;

		//TODO: imagenes? binary blob?

		throw new TipoDeColumnaInvalidaException(field);
	}

	static public Object valueFromResult(Columna col, ResultSet result, String label)
	throws SQLException, ORMException{ //TODO: unificar exceptions
		return switch(col.tipo()){
			case BOOL     -> result.getBoolean(label);
			case INT      -> result.getInt(label);
			case BIGINT   -> result.getLong(label);
			case FLOAT    -> result.getFloat(label);
			case DOUBLE   -> result.getDouble(label);
			case DECIMAL  -> result.getBigDecimal(label);
			case VARCHAR  -> result.getString(label);
			case TEXT     -> result.getString(label); //TODO: Clob?
			case DATETIME -> result.getTimestamp(label);
			case AUTO     -> throw new ORMException("Inés Perado"); //TODO:
		};
	}

	static public Object valueFromResult(Columna col, ResultSet result)
	throws SQLException, ORMException{
		return valueFromResult(col, result, col.nombre());
	}
}
