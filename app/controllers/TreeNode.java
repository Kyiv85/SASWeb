package controllers;

/**
 * Objeto para Casting de datos del cliente web
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class TreeNode {

	public String IDPeriodo;
	public String Modelo;
	public String Modelo_SC;
	public String DimRef;
	public String DriverName;
	public String DisplayRef;
	public String Padre_ID;
	public String tipoModulo;
	public String Contador;
	public String DisplayName;
	public String DQF;
	public String IDEscenario;
	public String Nivel;
	public String IntsctnRef;
	public String Costo;
	public String Padre;
	public String ID;
	public String DimName;
	public boolean isFolder;
	public TreeNode[] children;

	public TreeNode() {

	}

	public String toString() {

		return DisplayName;
	}

}
