package ORM.Queries;


sealed interface Query permits
	SelectQuery,
	AlterTableQuery,
	CreateTableQuery
{ }
