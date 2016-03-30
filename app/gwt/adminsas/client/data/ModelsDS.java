package gwt.adminsas.client.data;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSDataFormat;

/**
 * Proveedor de Datos de Periodo/Escenario.
 * 
 * Este proveedor es usado por la interfaz Administrador de SASWeb. Los datos
 * son provistos en formato JSON.
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class ModelsDS extends DataSource {

	private static ModelsDS instance = null;

	/**
	 * Instanciador por defecto(metodologia de Factories)
	 */
	public static ModelsDS getInstance() {
		if (instance == null) {
			instance = new ModelsDS("ModelsDS");
		}
		return instance;
	}

	/**
	 * Constructor del Proveedor de Datos
	 * 
	 * @param id
	 */

	private ModelsDS(String id) { 

		setID(id);
		setDataFormat(DSDataFormat.JSON);
		setRecordXPath("/response/data");

		DataSourceTextField modelIDField = new DataSourceTextField("modelID",
				"modelID", 128, true);
		modelIDField.setPrimaryKey(true);

		DataSourceTextField modelField = new DataSourceTextField("model",
				"model", 10, true);

		setFields(modelField, modelIDField);
		setDataURL("/SASWeb/json/admin/model");
	}
}
