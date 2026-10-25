package ORM;

import ORM.Annotations.ClavePrimaria;
import ORM.Annotations.GuardarComo;
import ORM.Annotations.NoSeGuarda;
import ORM.Clases.Tabla;
import ORM.Exceptions.*;
import ORM.Queries.AlterTableQuery;
import ORM.Queries.CreateTableQuery;
import ORM.Queries.SelectQuery;
import ORM.SQL.Columna;
import ORM.SQL.Schema;
import ORM.SQL.TipoColumna;

import javax.naming.OperationNotSupportedException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;


//TODO: indices

public class DB{

	private DB() throws OperationNotSupportedException{
		throw new OperationNotSupportedException(
			"Esta clase es estática, no hace falta instanciarla."
		);
	}

	private static final Connection conexion;
	private static final Map<Class<? extends Tabla>, Schema> tablas;


	static {
		var config_file = DB.class.getResourceAsStream("db.properties");
		var db_config = new Properties();
		try{
			db_config.load(config_file);
		}catch(IOException e){
			throw new RuntimeException("DB Config: " + e);
		}

		var host = db_config.getProperty("host");
		var port = db_config.getProperty("port");
		var db   = db_config.getProperty("db");
		var usr  = db_config.getProperty("usr");
		var pwd  = db_config.getProperty("pwd");

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
	 * agrega las clases que se usan en la DB. el ORM se encarga
	 * de que las tablas existan y sus campos estén actualizados.
	 * <p>
	 * <b>Cómo se usa:</b>
	 * </p>
	 * <pre>
	 * //las clases deben extender Tabla:
	 * class Pedido extends Tabla{}
	 * class Usuario extends Tabla{
	 *     private OneToMany<Pedido> pedidos;
	 * }
	 *
	 * //en main:
	 * DB.registrarTablas(Usuario.class, Pedido.class);
	 * </pre>
	 */
	@SafeVarargs
	public static void registrarTablas(Class<? extends Tabla> ...clases)
	throws ORMException, SQLException{
		for(final var clase : clases){
			if(tablas.containsKey(clase)) return;

			var schema = generarSchema(clase);
			tablas.put(clase, schema);

	//		Utils.prettyPrint(tablas);

		}
		//TODO: llamar a esto al final de todo
//		crearOActualizarTabla(clase, schema);

	}

	public static Object valorDePK(Tabla fila)
	throws TablaNoRegistradaException,
	       TablaModificadaException
	{
		final var schema = tablas.get(fila.getClass());
		if(schema == null) throw new TablaNoRegistradaException(fila.getClass());
		final var pk = schema.pk();
		pk.setAccessible(true);
		try{
			return pk.get(fila);
		} catch(IllegalAccessException e){
			// NOTE: esto no debería pasar nunca salvo que se haya
			//       modificado la DB mientras la app está funcionando.
			throw new TablaModificadaException(schema.tabla(), pk.getName());
		}
	}

	static public boolean guardar(Tabla fila){
		//TODO:
//		fila.guardado = true;
		return false;
	}

	static public SelectQuery buscar(
		Class<? extends Tabla> tabla
	){
		final var nombre_tabla = tablas.get(tabla).tabla();
		return new SelectQuery(nombre_tabla);
	}

	static public Tabla buscarPorPK(
		Class<? extends Tabla> tabla,
		Object pk
	){
		//TODO:
		return null;
	}

	static public void crearOActualizarTabla(
		Class<? extends Tabla> tabla,
		Schema schema
	) throws SQLException, ORMException{
		if(!existeTabla(schema)){
			crearTabla(schema);
		}else{
			final var db_schema = extraerSchemaDeDB(tabla, schema.tabla());
			if(db_schema.equals(schema)) return;

			actualizarTabla(schema, db_schema);
		}

	}

	private static void actualizarTabla(Schema schema_esperado, Schema schema_en_db){
//		final var schema_diff = diferenciasEntreSchemas(schema, db_schema);

	}

	private static AlterTableQuery diferenciasEntreSchemas(Schema esperado, Schema encontrado){
		//campos a crear

		//campos a modificar

		//campos a borrar

		//TODO:
		return new AlterTableQuery(null, null, null);
	}

	private static Schema extraerSchemaDeDB(
		Class<? extends Tabla> esperada,
		String tabla_nombre
	) throws SQLException, ORMException{
		final var meta_data = conexion.getMetaData();
		final var cols    = meta_data.getColumns(null, null, tabla_nombre, "%");
		final var pks     = meta_data.getPrimaryKeys(null, null, tabla_nombre);
		final var pk_name = pks.next() ? pks.getString("COLUMN_NAME") : null;

		final var columnas = new HashMap<Field, Columna>();
		Field  pk = null;

		while(cols.next()){
			//cols.getString("IS_AUTOINCREMENT") //TODO: usarlo
			//TODO: extraer estos strings a un enum porque es un asco esto asi carlitos
			final var column_name = cols.getString("COLUMN_NAME");
			final var es_pk = column_name.equals(pk_name);

			final var columna = new Columna(
				column_name,
				TipoColumna.fromString(cols.getString("TYPE_NAME")),
				cols.getString("IS_NULLABLE").equals("YES"),
				es_pk
			);

			//field equivalente
			final var field = buscarFieldParaColumna(esperada, columna);
			if(field == null){ //notama.. el campito notamá
				//TODO: borrarlo a la re mierda de la DB?
				throw new ColumnaAusenteException(
					columna.nombre(), esperada.toString()
				);
			}

			//primo calvo
			if(es_pk) pk = field;

			columnas.put(field, columna);
		}

		return new Schema(tabla_nombre, columnas, pk);
	}

	private static Field buscarFieldParaColumna(
		Class<? extends Tabla> tabla,
		Columna aguja
	) throws ORMException{
		final var columnas = columnasDeTabla(tabla);
		for(var entry : columnas.entrySet()){
			final var columna = entry.getValue();
			final var field = entry.getKey();
			if(columna.nombre().equals(aguja.nombre())){
				return field;
			}
		}
		return null;
	}

	private static void crearTabla(Schema schema)
	throws SQLException{
		final var statement = conexion.createStatement();
		final var query = new CreateTableQuery(schema);
		statement.executeUpdate(query.toString());
		statement.close();
	}

	static public boolean existeTabla(Schema schema) throws SQLException{
		final var statement = conexion.createStatement();
		final var sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '%s';";
		final var result = statement.executeQuery(sql.formatted(schema.tabla()));
		final var success = result.next() && result.getInt(1) > 0;
		statement.close();
		return success;
	}

	//priv

	static private Schema generarSchema(
		Class<? extends Tabla> tabla
	) throws ORMException{
		final var columnas = columnasDeTabla(tabla);
		final var pk = columnas
			.entrySet()
			.stream()
			.filter(es -> es.getValue().pk())
			.toList()
			.getFirst()
			.getKey();
		return new Schema(camelToSnake(tabla.getSimpleName()), columnas, pk);
	}

	static private HashMap<Field, Columna> columnasDeTabla(
		Class<? extends Tabla> tabla
	) throws ORMException{
		final var columnas = new HashMap<Field, Columna>();
		//TODO: getDeclaredFields excluye campos heredados
		final var fields = tabla.getDeclaredFields();

		Field clave_primaria = null;

		//inspeccionar todos los campos
		for(var field : fields){

			//saltear static
			if(Modifier.isStatic(field.getModifiers())) continue;

			final var es_guardable = field.getAnnotation(NoSeGuarda.class)    == null;
			final var es_pk        = field.getAnnotation(ClavePrimaria.class) != null;
			final var es_nullable  = !es_pk && !field.getType().isPrimitive();

			if(!es_guardable && es_pk){
				throw new PKNoGuardableException(tabla, field);
			}

			if(!es_guardable) continue;

			if(es_pk && clave_primaria != null){
				throw new PKsMultiplesException(
					tabla, clave_primaria, field
				);
			}

			if(es_pk) clave_primaria = field;

			columnas.put(field, new Columna(
				nombreDeColumna(field),
				tipoDeColumna(field),
				es_nullable,
				es_pk
			));

			//TODO: resolver el tema de las claves foraneas
		}

		if(clave_primaria == null){
			throw new PKAusenteException(tabla);
		}

		return columnas;
	}

	static private TipoColumna tipoDeColumna(Field field)
	throws TipoDeColumnaInvalidaException{
		final var nota = field.getAnnotation(GuardarComo.class);
		final var tipo = field.getType();

		if(nota != null
		&& nota.tipo() != null
		&& nota.tipo() != TipoColumna.AUTO){
			return nota.tipo();
		}

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

	static private String nombreDeColumna(Field field){
		return camelToSnake(field.getName());
	}

	static private String camelToSnake(String str){
		return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}
}
