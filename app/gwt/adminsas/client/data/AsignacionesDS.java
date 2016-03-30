package gwt.adminsas.client.data;

import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.DSProtocol;

/**
 * Proveedor de Datos de la vista de Asignaciones
 * 
 * Este proveedor es usado por la interfaz Administrador de SASWeb. Los datos
 * son provistos en formato JSON.
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class AsignacionesDS extends RestDataSource {

	private static AsignacionesDS instance = null;

	/**
	 * Instanciador por defecto(metodologia de Factories)
	 */
	public static AsignacionesDS getInstance() {
		if (instance == null) {
			instance = new AsignacionesDS("AsignacionesDS");
		}
		return instance;
	}

	/**
	 * Constructor del Proveedor de Datos
	 * 
	 * @param id
	 */

	private AsignacionesDS(String id) { 

		setID(id);
		setDataFormat(DSDataFormat.JSON);
		setDataProtocol(DSProtocol.POSTMESSAGE);

		DataSourceTextField refField = new DataSourceTextField("referencia",
				"Referencia");
		refField.setPrimaryKey(true);

		DataSourceTextField nameField = new DataSourceTextField("nombre",
				"Nombre", 120);
		DataSourceTextField dqfField = new DataSourceTextField("dqf",
				"Driver Quantity");
		DataSourceTextField tipoField = new DataSourceTextField("tipoModulo",
				"Tipo");
		
		DataSourceTextField driverField = new DataSourceTextField("conductor",
		"Conductor");

		setFields(tipoField, nameField, refField, dqfField, driverField);
		setDataURL("/SASWeb/json/admin/asignaciones");
	}

}
