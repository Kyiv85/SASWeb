package gwt.adminsas.client.data;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.json.client.JSONArray;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.XMLTools;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.DSProtocol;

public class AsignacionesDestinoDS extends RestDataSource {

	private static AsignacionesDestinoDS instance = null;
	private JSONArray jerarquia;

	/**
	 * Instanciador por defecto(metodologia de Factories)
	 */
	public static AsignacionesDestinoDS getInstance() {
		if (instance == null) {
			instance = new AsignacionesDestinoDS("AsignacionesDestinoDS");
		}
		return instance;
	}

	/**
	 * Constructor del Proveedor de Datos
	 * 
	 * @param id
	 */

	private AsignacionesDestinoDS(String id) {

		setID(id);
		setDataFormat(DSDataFormat.JSON);
		setDataProtocol(DSProtocol.POSTMESSAGE);

		DataSourceTextField displayName = new DataSourceTextField(
				"DisplayName", "Nombre", 64);

		DataSourceTextField ID = new DataSourceTextField("ID",
				"Display Reference", 128);

		DataSourceTextField displayRef = new DataSourceTextField("DisplayRef",
				"Referencia", 128);
		displayRef.setPrimaryKey(true);

		DataSourceTextField padre = new DataSourceTextField("Padre", "Padre");
		padre.setRootValue("");
		padre.setForeignKey("DisplayRef");

		DataSourceTextField dimName = new DataSourceTextField("DimName",
				"Dimension Name", 128);

		DataSourceTextField dimRef = new DataSourceTextField("DimRef",
				"Dimension Ref", 128);

		DataSourceTextField tipo = new DataSourceTextField("tipoModulo",
				"Tipo", 128);

		DataSourceTextField periodo = new DataSourceTextField("IDPeriodo",
				"Periodo", 128);
		DataSourceTextField escenario = new DataSourceTextField("IDEscenario",
				"Escenario", 128);

		DataSourceTextField Padre_ID = new DataSourceTextField("Padre_ID",
				"Padre ID", 128);

		setFields(ID, Padre_ID, displayName, displayRef, dimName, dimRef,
				padre, tipo, periodo, escenario);

		setDataURL("/SASWeb/json/admin/data");
	}

	protected void transformResponse(DSResponse response, DSRequest request,
			Object data) {

		JSONArray jer = XMLTools.selectObjects(data, "/response/jerarquia");

		if (jer != null && jer.size() > 0)
			jerarquia = jer;

		super.transformResponse(response, request, data);
	}

	public JSONArray getJerarquia() {

		return jerarquia;
	}

}
