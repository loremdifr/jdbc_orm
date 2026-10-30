# ponja

pequenho ORM na Java


## TODOs


#### TIPOS:
- agregar LocalDate y DATE column
- guardar Enums como VARCHAR
  - Checkear que todos los enums guardados ne la DB sean validos despues deregistrarTablas y tirar uan excpetion
- fix LocalDateTime.. no funciona con timestamp como yo creia


#### QUERIES:
- SelectQuery hacer generic cosa que DB.buscar(clase) devuelva SelectQuery<T> y no haya que castear por todos lados
  - toList() tambien tendria que ser generica?
- agregar DB.query("") para poder hacer queries manuales



#### Tabla base
- mover id a Tabla y que sea todo eso automatico
  - quitar @ClavePrimaria
- agregar columnas de audit a Tabla:
  - creacion
  - modificacion
  - borrado
    - agregar borrado como un filtro por defecto?
  - quizas una config al ORM para decirle cual es la tabla "usuario" y poder agregar modificado_por y creado_por?
  - 
- dirty flag (guardado)
  - guardar snapshot de todos los campos, ordenados por nombre
  - guardado() deja de ser una variable y mejor compara campos con el snapshot. hasheado?


####  relaciones de tablas
  - ManyToOne: la fk se autocrea y la matiene el orm
  - OneToMany: lazy list en el get. Si la columna inversa no tiene la relation, tirar error.
  - ManyToMany: .. por ahora no
  

#### EXCEPTIONS:

- Mergear las Exceptions porque es un despelote todo
