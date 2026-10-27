package ORM.SQL;

import ORM.Exceptions.TipoDeColumnaInesperadoException;
import java.sql.Types;

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
}
