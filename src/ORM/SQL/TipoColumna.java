package ORM.SQL;

import ORM.Exceptions.TipoDeColumnaInesperadoException;

public enum TipoColumna{
	AUTO    (""),
	BOOL    ("BOOL"),
	INT     ("INT"),
	BIGINT  ("BIGINT"),
	FLOAT   ("FLOAT"),
	DOUBLE  ("DOUBLE"),
	DECIMAL ("DECIMAL"),
	VARCHAR ("VARCHAR(255)"), //TODO: parametrizar?
	TEXT    ("TEXT"),
	DATETIME("DATETIME"),
	;

	final private String sqlString;
	TipoColumna(String sqlString){ this.sqlString = sqlString; }
	public String sqlString(){ return sqlString; }

	static public TipoColumna fromString(String str)
	throws TipoDeColumnaInesperadoException{
		for(var tipo_columna : values()){
			if(tipo_columna.sqlString().equals(str)) return tipo_columna;
		}
		throw new TipoDeColumnaInesperadoException(str);
	}

}
