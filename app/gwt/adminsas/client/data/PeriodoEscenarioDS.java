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
public class PeriodoEscenarioDS extends DataSource {

	private static PeriodoEscenarioDS instance = null;

	/**
	 * Instanciador por defecto(metodologia de Factories)
	 */
	public static PeriodoEscenarioDS getInstance() {
		if (instance == null) {
			instance = new PeriodoEscenarioDS("PeriodoEscenarioDS");
		}
		return instance;
	}

	/**
	 * Constructor del Proveedor de Datos
	 */

	private PeriodoEscenarioDS(String id) {

		setID(id);
		setDataFormat(DSDataFormat.JSON);
		setRecordXPath("/response/data");

		DataSourceTextField periodoField = new DataSourceTextField(
				"periodoScenario", "periodoScenario", 128, true);
		periodoField.setPrimaryKey(true);

		DataSourceTextField escenarioField = new DataSourceTextField(
				"periodoScenarioID", "periodoScenarioID", 10, true);

		setFields(periodoField, escenarioField);
		setDataURL("/SASWeb/json/admin/periodscenario");
	}
}
