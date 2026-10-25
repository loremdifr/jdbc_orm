package ORM;

import javax.naming.OperationNotSupportedException;

public class Utils{
	private Utils() throws OperationNotSupportedException{
		throw new OperationNotSupportedException(
			"Esta clase es estática, no hace falta instanciarla."
		);
	}

	public static void prettyPrint(Object ojeto){
		final var str = ojeto.toString()
			.replace("],", "]")
			.replace("},", "}")
			.replace(", ", ",");
		final var tab = "︙\t";
		int tab_level = 0;
		for(int i = 0; i <  str.length(); i++){
			final var chr = str.charAt(i);
			switch(chr){
				case '[', '{' -> {
					System.out.println(chr);
					System.out.print(tab.repeat(++tab_level));
				}
				case ']', '}' -> {
					System.out.println();
					System.out.println(tab.repeat(--tab_level) + chr);
					System.out.print(tab.repeat(tab_level));
				}
				case ',' -> {
					System.out.println(chr);
					System.out.print(tab.repeat(tab_level));
				}
				default -> System.out.print(chr);
			}
		}
	}
}
