package gwt.adminsas.client.data;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSDataFormat;

/**
 * Proveedor de Datos de Conductores
 * 
 * Este proveedor es usado por la interfaz Administrador de SASWeb. Los datos
 * son provistos en formato JSON.
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class ConductorDS extends DataSource {

	private static ConductorDS instance = null;

	/**
	 * Instanciador por defecto(metodologia de Factories)
	 */
	public static ConductorDS getInstance() {
		if (instance == null) {
			instance = new ConductorDS("ConductorDS");
		}
		return instance;
	}

	/**
	 * Constructor del Proveedor de Datos
	 * 
	 * @param id
	 */

	private ConductorDS(String id) { 

		setID(id);
		setDataFormat(DSDataFormat.JSON);
		setRecordXPath("/response/data");

		DataSourceTextField conductorIDField = new DataSourceTextField(
				"conductorID", "conductorID", 128, true);
		conductorIDField.setPrimaryKey(true);

		DataSourceTextField conductorField = new DataSourceTextField(
				"conductor", "conductor", 10, true);

		setFields(conductorField, conductorIDField);
		setDataURL("/SASWeb/json/admin/conductor");
	}
}
