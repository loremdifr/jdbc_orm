import ORM.Exceptions.ORMException;
import ORM.DB;

import java.sql.SQLException;

//TODO: queries
//TODO: softDelete trait o algo asi?
//TODO: lastModified
//TODO: transactions
//TODO: asociaciones
//TODO: enums -> store as varchar (startup check that nothing invalid is stored in the db!)

void main() throws ORMException, SQLException{

	//requisito para usar esto:
	// - copiar el archivo db.properties.default a db.properties
	// - modificarlo con los datos de acceso a su db
	// - la db no se crea sola, asi que hay que crearla en blanco

	//registro clases en SQL y se auto-crean las tablas en la DB:
	DB.registrarTablas(
		Usuario.class,
		Empresa.class
	);
	// si le cambias los datos a la clase Usuario, se van a mantener
	// actualizados en la DB solitos.

	//guardar en la db es asi:
	var mi_usuario = new Usuario(
		"Jared",
		19,
		"What up, I'm Jared, I'm 19, and I never fucking learned how to read"
	);
	System.out.println("usuario: " + mi_usuario);
	mi_usuario.guardar();
	System.out.println("guardado: " + mi_usuario.guardado());

	var pk = mi_usuario.PK(); //una vez guardado, la clave primaria se actualiza
	System.out.println("pk: " + pk);

	try{
		//Despues puedo buscarlo directamente por PK:
		var usuario_especifico = DB.buscarPorPK(Usuario.class, pk);
		System.out.println("desde la db: " + usuario_especifico);
	}catch(Exception e){}

	//O puedo buscar lo que quiera:
//	var jared = SQL.buscar(Usuario.class)
////		.where("nombre", "=", "Jared")
//		.where("nombre", "LIKE", "J%")
//		.andWhere("edad", ">", 18)
//		.first(); //le mete limit 1, y te devuelve solo el primer resultado

	//llamando a toList() se obtiene un ArrayList
//	var usuarios = SQL.buscar(Usuario.class)
//		.where("nombre", "LIKE", "J%")
//		.orWhere("nombre", "IN", "Pedro", "Pablo")
//		.groupBy("edad")
//		.limit(12)
//		.orderBy("nombre", "ASC")
//		.toList();

	// las funciones de SQL tienen su propio metodo:
//	var total_usuarios = SQL.buscar(Usuario.class).count("*");
//	var edades_promedio = SQL.buscar(Usuario.class).avg("edad");
//	var chikitito = SQL.buscar(Usuario.class).min("edad");
//	var grandote = SQL.buscar(Usuario.class).max("edad");
	//	sum
	//	distinct


	// queries manuales
//	var todos = SQL.query("SELECT * FROM usuario;").toList();

	//


	//se pueden agregar relaciones
//	SQL.relacionarTablas(Usuario.class, SQL.belongsTo, Empresa.class);
//	SQL.relacionarTablas(Empresa.class, SQL.hasMany, Usuario.class);
//	SQL.relacionarTablas(Usuario.class, SQL.hasOne, Perfil.class);
//	SQL.relacionarTablas(Usuario.class, SQL.belongsToMany, Usuario.class, "amigos?");



}
