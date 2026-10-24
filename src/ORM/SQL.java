package ORM;

import javax.naming.OperationNotSupportedException;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.Method;
import java.util.Properties;


public class SQL{

	private SQL() throws OperationNotSupportedException{
		throw new OperationNotSupportedException(
			"Esta clase es estática, no hace falta instanciarla."
		);
	}

	private static Connection conexion;
	private static Map<String, String> tablas;

	//todas las tablas tiene id como pk
	//TODO: indices
	public enum TipoColumna{ AUTO, INT, VARCHAR, TEXT, DATETIME, BOOL }
	private record Columna(String nombre, TipoColumna tipo){}
	private record Schema(
		String tabla,
		ArrayList<Columna> columnas,
		Columna PK
	){}

	static {
		var config_file = SQL.class.getResourceAsStream("db.properties");
		var db_config = new Properties();
		try{
			db_config.load(config_file);
		}catch(IOException e){
			throw new RuntimeException("DB Config: " + e);
		}

		var host = db_config.getProperty("localhost");
		var port = db_config.getProperty("3306");
		var db   = db_config.getProperty("test");
		var usr  = db_config.getProperty("admin");
		var pwd  = db_config.getProperty("admin");

		try{
			var url_template = "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC";
			conexion = DriverManager.getConnection(
				url_template.formatted(host, port, db), usr, pwd
			);
		}catch(SQLException e){
			throw new RuntimeException("SQL: " + e);
		}

		tablas = new HashMap<>();
	}

	/**
	 * agregar clases que se usan en la DB. el ORM se encarga
	 * de que la tabla exista y sus campos estén actualizados.
	 * <p>
	 * <b>Cómo se usa:</b>
	 * </p>
	 * <pre>
	 * //clase o record debe implementar ORM.Guardable:
	 * class Usuario implements ORM.Guardable{}
	 * record Pedido(..) implements ORM.Guardable{}
	 *
	 * //en main:
	 * ORM.SQL.registrarTable(Usuario.class);
	 * ORM.SQL.registrarTable(Pedido.class);
	 * </pre>
	 */
	static public boolean registrarTabla(Class<? extends Tabla> guardable){
		final var tabla = guardable.toString();
		if(tablas.containsKey(tabla)) return false;

		//armar schema
		var schema = generarSchema(guardable);

		//crear tabla si no existe


		//verificar diferencias entre campos, crear si no existen


		tablas.put(tabla, tabla);
		return true;

	}

	static public ArrayList<Tabla> buscar(
		Class<? extends Tabla> tabla
	){
		//TODO:
		return null;
	}

	static public Tabla buscarPorId(
		Class<? extends Tabla> tabla,
		long id
	){
		//TODO:
		return null;
	}


	//priv

	static private ArrayList<Method> gettersDeClase(
		Class<? extends Tabla> guardable
	){
		var getters = new ArrayList<Method>();

		for(Method metodo : guardable.getMethods()){
			final var nombre     = metodo.getName();
			final var tipo       = metodo.getReturnType();
			final var params     = metodo.getParameterTypes();
			final var modifiers  = metodo.getModifiers();

			if(!Modifier.isPublic(modifiers)) continue;
			if(tipo.equals(void.class))       continue;
			if(params.length > 0)             continue;
			if(nombre.equals("getClass"))     continue;

			//getter que empieza con get
			final boolean es_get = (
				nombre.startsWith("get")
			 && nombre.length() > 3
			 && Character.isUpperCase(nombre.charAt(3))
			);

			//getter que empieza con is
			final boolean es_is = (
				nombre.startsWith("is")
			 && nombre.length() > 2
			 && Character.isUpperCase(nombre.charAt(2))
			);

			if(es_get || es_is){
				getters.add(metodo);
			}
		}

		return getters;
	}


	static private Schema generarSchema(
		Class<? extends Tabla> guardable
	){
		var pk      = new Columna("id", TipoColumna.INT);
		var campos  = new ArrayList<Columna>();
		var getters = gettersDeClase(guardable);

		for(var getter : getters){
			var nombre = getter.getName();
			var tipo = tipoDelGetter(getter);
			campos.add(new Columna(nombre, tipo));
		}

		return new Schema(guardable.toString(), campos, pk);
	}

	static private TipoColumna tipoDelGetter(Method getter)
	throws IllegalArgumentException {
		final var tipo = getter.getReturnType();
		if(tipo == int.class)     return TipoColumna.INT;
		if(tipo == Integer.class) return TipoColumna.INT;
		if(tipo == String.class)  return TipoColumna.VARCHAR;
		if(tipo == boolean.class) return TipoColumna.BOOL;
		//TODO:

		throw new IllegalArgumentException(
			"Tipo del campo `%s` no soportado.".formatted(
				getter.getName()
			)
		);
	}


}
