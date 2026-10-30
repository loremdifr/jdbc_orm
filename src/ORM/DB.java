package ORM;

import ORM.Annotations.*;
import ORM.Clases.*;
import ORM.Exceptions.*;
import ORM.Queries.*;
import ORM.SQL.*;

import javax.naming.OperationNotSupportedException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	private static final String nombre_db;


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
			final var url_template = "jdbc:mysql://%s:%s/%s?useSSL=false";
			conexion = DriverManager.getConnection(
				url_template.formatted(host, port, db), usr, pwd
			);
		}catch(SQLException e){
			throw new RuntimeException("SQL: " + e);
		}

		nombre_db = db;
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

		if(!tablas.isEmpty()) throw new NoLeisteElReadmeException();

		for(final var clase : clases){
			if(tablas.containsKey(clase)) return;

			var schema = generarSchema(clase);
			tablas.put(clase, schema);

	//		Utils.prettyPrint(tablas);
		}

		for(var entry : tablas.entrySet()){
			final var clase  = entry.getKey();
			final var schema = entry.getValue();
			crearOActualizarTabla(clase, schema);
		}

	}

	public static Object valorDeColumna(Tabla fila, Columna columna)
	throws ORMException{
		final var field = DB.buscarFieldParaColumna(fila.getClass(), columna);
		Objects.requireNonNull(field).setAccessible(true);
		try{
			return field.get(fila);
		} catch(IllegalAccessException e){
			// NOTE: esto no debería pasar nunca salvo que se haya
			//       modificado la DB mientras la app está funcionando.
			final var schema = tablas.get(fila.getClass());
			if(schema == null) throw new TablaNoRegistradaException(fila.getClass());
			throw new TablaModificadaException(schema.tabla(), field.getName());
		}
	}


	static public boolean guardar(Tabla fila) throws SQLException, ORMException, IllegalAccessException{
		if(fila.guardado()) return true;
		final var tabla     = tablas.get(fila.getClass());
		final var query     = new InsertIntoQuery(tabla, fila);
		final var statement = query.toPreparedStatement(conexion);
		final var result    = statement.executeUpdate() > 0;

		//rellenar PK si se creo
		if(result && fila.getId() == null){
			final var generated_keys = statement.getGeneratedKeys();
			final var schema   = tablas.get(fila.getClass());
			final var pk_field = schema.pk();
			final var col      = schema.columnas().get(pk_field);

			generated_keys.next(); //TODO: assert

			final var nueva_id = TipoColumna.valueFromResult(col, generated_keys, "GENERATED_KEY");
			pk_field.setAccessible(true);
			pk_field.set(fila, nueva_id);
		}

		return result;
	}

	static public <T extends Tabla> ArrayList<T> ejecutarQuery(
		Query query, Class<T> tabla
	) throws SQLException,
			 ORMException,
			 InvocationTargetException,
			 InstantiationException,
			 IllegalAccessException
	{
		final var statement = query.toPreparedStatement(conexion);
		final var results   = statement.executeQuery();
		return resultSetAFilas(results, tabla);
	}

	static private <T extends Tabla> ArrayList<T> resultSetAFilas(
		ResultSet results, Class<T> tabla
	) throws SQLException,
		InvocationTargetException,
		InstantiationException,
		IllegalAccessException, ORMException{
		final var filas = new ArrayList<T>();
		final var schema = tablas.get(tabla);
		while(results.next()){
			try{
				final var constructor = tabla.getConstructor();
				constructor.setAccessible(true);
				final var instancia = constructor.newInstance();
				//rellenar ojeto
				for(var entry : schema.columnas().entrySet()){
					final var field = entry.getKey();
					final var col   = entry.getValue();
					final var valor = TipoColumna.valueFromResult(col, results);
					field.setAccessible(true);
					field.set(instancia, valor);
				}
				filas.add(tabla.cast(instancia));

			}catch(NoSuchMethodException e){
				//TODO: mover a su propia exception, checkear en registrarTablas
				throw new ORMException(
					"Las clases que heredan de Tabla deben implementar un constructor sin argumentos."
				);
			}

		}
		return filas;
	}

	static public SelectQuery buscar(Class<? extends Tabla> tabla){
		return new SelectQuery(tabla);
	}

	static public Tabla buscarPorId(
		Class<? extends Tabla> tabla,
		Integer id
	) throws ORMException, SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException{
		final var schema = tablas.get(tabla);
		if(schema == null) throw new TablaNoRegistradaException(tabla);
		return buscar(tabla).where("id", "=", id).first();
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

	private static void actualizarTabla(
		Schema schema_esperado,
		Schema schema_en_db
	) throws ORMException{
		if(schema_en_db.equals(schema_esperado)) return; //pero si estas igual

		if(!schema_en_db.tabla().equals(schema_esperado.tabla())){
			//cambio el nombre????
			throw new ORMException("Esto no deberia ser posible.");
		}

		//diff columnas
		final var cols_esperadas   = schema_esperado.columnas();
		final var cols_en_db       = schema_en_db.columnas();
		final var cols_faltantes   = Utils.diffColumnas(cols_esperadas, cols_en_db);
		final var cols_sobrantes   = Utils.diffColumnas(cols_en_db, cols_esperadas);
		final var cols_a_modificar = new HashMap<String, Columna>();

		for(final var entry : cols_esperadas.entrySet()){
			final var field = entry.getKey();
			final var col   = entry.getValue();
			if(!cols_en_db.containsKey(field))    continue;
			if(cols_faltantes.containsKey(field)) continue;
			if(cols_sobrantes.containsKey(field)) continue;

			final var col_en_db = cols_en_db.get(field);

			if(!col_en_db.nombre().equals(col.nombre())
			|| !col_en_db.tipo().equals(col.tipo())
			|| col_en_db.pk() != col.pk()
			|| col_en_db.not_null() != col.not_null()){
				cols_a_modificar.put(col_en_db.nombre(), col);
			}
		}

		final var super_query = new AlterTableQuery(
			schema_esperado,
			!schema_en_db.pk().equals(schema_esperado.pk()),
			new ArrayList<>(cols_faltantes.values().stream().toList()),
			new ArrayList<>(cols_sobrantes.values().stream().toList()),
			cols_a_modificar
		);

		try{
			final var statement = super_query.toPreparedStatement(conexion);
			final var result    = statement.executeUpdate() > 0;
		}catch(SQLException e){
			throw new ORMException("Algo salió terriblemente mal :D");
		}

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
		Field pk = null;

		while(cols.next()){
			//cols.getString("IS_AUTOINCREMENT") //TODO: usarlo
			//TODO: extraer estos strings a un enum porque es un asco esto asi carlitos
			final var column_name = cols.getString("COLUMN_NAME");
			final var es_pk       = column_name.equals(pk_name);
			final var tipo_col    = TipoColumna.fromSqlType(cols.getInt("DATA_TYPE"));
			final var columna     = new Columna(
				column_name,
				tipo_col,
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
		final var sql = """
						SELECT COUNT(*) FROM information_schema.tables
						WHERE table_schema = '%s'
						AND table_name = '%s';
						""";
		final var result = statement.executeQuery(sql.formatted(nombre_db, schema.tabla()));
		final var success = result.next() && result.getInt(1) > 0;
		statement.close();
		return success;
	}


	public static Columna getColumna(Schema schema, String nombre_columna)
	throws ColumnaAusenteException{
		for(var columna : schema.columnas().values()){
			if(columna.nombre().equals(nombre_columna)) return columna;
		}
		throw new ColumnaAusenteException(nombre_columna, schema.tabla());
	}

	public static Columna getColumna(String nombre_tabla, String nombre_columna)
	throws ColumnaAusenteException{
		//TODO: probablemente necesito un mapa de string a tabla
		//      para que esto sea mas rapido, porque se llama desde las queries
		for(var schema : tablas.values()){
			if(!schema.tabla().equals(nombre_tabla)) continue;
			return getColumna(schema, nombre_columna);
		}
		throw new ColumnaAusenteException(nombre_columna, nombre_tabla);
	}

	public static Schema schemaDeTabla(Class<? extends Tabla> tabla){
		return tablas.get(tabla);
	}

	public static long hashearFila(Tabla fila) throws ORMException{
		final var cols = columnasDeTabla(fila.getClass());
		final var cols_en_orden = cols.values().stream().sorted().toList();
		long hashcode = 6767676767676767L;

		for(var col : cols_en_orden){
			final var valor = valorDeColumna(fila, col);
			hashcode *= 31;
			hashcode += Objects.hashCode(valor);
		}
		return hashcode;
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
		try{
			final var clave_primaria = Tabla.class.getDeclaredField("id");

			Class<?> curr_class = tabla;
			while(curr_class != null && curr_class != Object.class){
				for(var field : curr_class.getDeclaredFields()){

					//saltear static
					if(Modifier.isStatic(field.getModifiers())){
						continue;
					}

					//saltear no guardables
					if(field.getAnnotation(NoSeGuarda.class) != null){
						continue;
					}

					//ver si no se intento pisar la id
					if(field.getName().equals("id")
					&& curr_class != Tabla.class){
						//TODO: hacer exception unica
						throw new ORMException("Se intentó redefinir 'id' en '%s'".formatted(
							tabla.toString()
						));
					}

					columnas.put(field, new Columna(
						nombreDeColumna(field),
						tipoDeColumna(field),
						!field.getType().isPrimitive(),
						field.equals(clave_primaria)
					));

					//TODO: resolver el tema de las claves foraneas
				}
				curr_class = curr_class.getSuperclass();
			}

		}catch(NoSuchFieldException e){
			//jamas se tira esta exception porque Tabla tiene el campo
		}

		return columnas;
	}

	static private TipoColumna tipoDeColumna(Field field)
	throws TipoDeColumnaInvalidaException{
		final var nota = field.getAnnotation(GuardarComo.class);

		if(nota != null
		&& nota.tipo() != null
		&& nota.tipo() != TipoColumna.AUTO){
			return nota.tipo();
		}

		return TipoColumna.fromField(field);
	}

	static private String nombreDeColumna(Field field){
		return camelToSnake(field.getName());
	}

	static private String camelToSnake(String str){
		return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}


}
