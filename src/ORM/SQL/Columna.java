package ORM.SQL;

public record Columna(
	String nombre,
	TipoColumna tipo,
	boolean not_null,
	boolean pk
){}
