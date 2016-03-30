package gwt.adminsas.client.data;

import java.util.HashMap;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RestDataSource;
import com.smartgwt.client.data.ResultSet;
import com.smartgwt.client.data.XMLTools;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.util.JSOHelper;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * Proveedor de Datos de la vista MultiDimensión
 * 
 * Este proveedor es usado por la interfaz Administrador de SASWeb. Los datos
 * son provistos en formato JSON.
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class MultiDimensionDS extends RestDataSource {

	private static MultiDimensionDS instance = null;
	private JSONArray jerarquia;
	private TreeGrid tg;

	/**
	 * Instanciador por defecto(metodologia de Factories)
	 */
	public static MultiDimensionDS getInstance() {
		if (instance == null) {
			instance = new MultiDimensionDS("MultiDimensionDS");
		}
		return instance;
	}

	public void setTree(TreeGrid tg) {
		this.tg = tg;
	}

	
	/**
	 * Constructor del Proveedor de Datos
	 * 
	 * @param id
	 */

	public MultiDimensionDS(String id) { 

		setID(id);
		setDataFormat(DSDataFormat.JSON);
		setDataProtocol(DSProtocol.POSTMESSAGE);

		DataSourceTextField displayName = new DataSourceTextField(
				"DisplayName", "Display Name", 64);

		// Valor para visualizacion
		DataSourceTextField ID = new DataSourceTextField("ID",
				"Display Reference", 64);

		DataSourceTextField displayRef = new DataSourceTextField("DisplayRef",
				"IntsctnRef", 128);
		displayRef.setPrimaryKey(true);

		DataSourceTextField padre = new DataSourceTextField("Padre", "Padre IntsctnRef");
		padre.setRootValue("");
		padre.setForeignKey("DisplayRef");

		DataSourceTextField dimName = new DataSourceTextField("DimName",
				"Dimension Ref.", 128);
		
		DataSourceTextField dimRef = new DataSourceTextField("DimRef",
				"Dimension Name", 128);
		
		DataSourceTextField nivel = new DataSourceTextField("Nivel", "Nivel",
				128);

		DataSourceTextField modelo = new DataSourceTextField("Modelo",
				"Modelo", 128);
		
		DataSourceTextField tipo = new DataSourceTextField("tipoModulo",
				"Tipo", 128);

		DataSourceTextField periodo = new DataSourceTextField("IDPeriodo",
				"Periodo", 128);
		DataSourceTextField escenario = new DataSourceTextField("IDEscenario",
				"Escenario", 128);

		DataSourceTextField Padre_ID = new DataSourceTextField("Padre_ID",
				"Padre", 128);

		DataSourceTextField editNow = new DataSourceTextField("editNow",
				"editNow", 128);

		setFields(ID, Padre_ID, displayName, displayRef, dimName,
				dimRef, padre, nivel, modelo,
				tipo, periodo, escenario, editNow);

		setDataURL("/SASWeb/json/admin/data");
	}

	protected void transformResponse(DSResponse response, DSRequest request,
			Object data) {

		JSONArray jer = XMLTools.selectObjects(data, "/response/jerarquia");

		if (jer != null && jer.size() > 0)
			jerarquia = jer;
		
		super.transformResponse(response, request, data);
	}
	
	@Override
	protected Object transformRequest(DSRequest dsRequest) {
		
		if (dsRequest.getOperationType().equals(DSOperationType.UPDATE)){
			
			String dimName = JSOHelper.getAttribute(dsRequest.getData(), "DimName");
			
			// es reparenting
			if(dimName != null){
				dsRequest.setOldValues(new HashMap<String, String>());
			}
		}else if (dsRequest.getOperationType().equals(DSOperationType.FETCH)){
			
			// Colocar dimActual en DSRequest basado en Padre
			
			String dim = "";
			String currentPadre = JSOHelper.getAttribute(dsRequest.getData(), "Padre");
			
			// obtenemos todo el arbol
			Tree tr = tg.getData();
			TreeNode[] data = tr.getAllNodes();
			
			for (int i = 0; i < data.length; i++) {
			
				// Si de la lista de nodos conseguimos el que corresponde
				// al nodo que estamos abriendo
				if(currentPadre.equalsIgnoreCase(data[i].getAttribute("DisplayRef"))){
				
					// Esta es nuestra currentDim
					dim = data[i].getAttribute("DimName");
				}
			}
						
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("dimActual", dim);
			
			dsRequest.setParams(params);
		}
		
		return super.transformRequest(dsRequest);
	}


	public JSONArray getJerarquia() {
		return jerarquia;
	}

}
