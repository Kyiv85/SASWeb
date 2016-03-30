package gwt.adminsas.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import gwt.adminsas.client.data.AsignacionesDS;

import com.allen_sauer.gwt.log.client.Log;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.data.ResultSet;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.grid.events.EditorExitEvent;
import com.smartgwt.client.widgets.grid.events.EditorExitHandler;

/**
 * Vista de Asignaciones
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class AsignacionesView extends ListGrid {

	private AsignacionesDS asignacionDS;
	private ListGridField refField;
	private ListGridField dqfField;

	private ListGridField tipoField;
	private ListGridField driverField;

	/**
	 * Constructor por defecto
	 * 
	 */
	public AsignacionesView() {

		asignacionDS = AsignacionesDS.getInstance();

		// Configuración basica de la vista MultiDimensión
		setDataSource(asignacionDS);
		setCanEdit(true);
		setSelectionType(SelectionStyle.SINGLE);

		setLoadingDataMessage("Cargando información del Servidor");
		setAutoFetchData(false);

		setShowEmptyMessage(true);
		setEmptyMessage("<br>Haz click en una <b>hamburguesa</b> para ver las Asignaciones");

		setShowAllRecords(false);

		setCanSelectText(true);
		setWidth100();
		setHeight100();

		ListGridField nameField = new ListGridField("nombre", "Nombre", 120);
		nameField.setCanEdit(false);

		refField = new ListGridField("referencia", "Referencia");
		refField.setCanEdit(false);

		driverField = new ListGridField("conductor", "Conductor");
		driverField.setHidden(true);

		dqfField = new ListGridField("dqf", "Driver Quantity");

		tipoField = new ListGridField("tipo", "Tipo", 40);
		tipoField.setCanEdit(false);

		tipoField.setType(ListGridFieldType.IMAGE);
		tipoField.setImageURLPrefix("/SASWeb/app/img/botones/");
		tipoField.setImageURLSuffix(".png");

		addDataArrivedHandler(new DataArrivedHandler() {
			@Override
			public void onDataArrived(DataArrivedEvent event) {
		
				ListGridRecord[] allrecords = getRecords();
				
				for (int i = 0; i < allrecords.length; i++) {
				
					if (allrecords[i].getAttribute("tipoModulo").equalsIgnoreCase("RESOURCE")) {

						allrecords[i].setAttribute("tipo", "/SASWeb/app/img/botones/recurso");
					} else if (allrecords[i].getAttribute("tipoModulo").equalsIgnoreCase(
							"COSTOBJECT")) {

						allrecords[i].setAttribute("tipo",
								"/SASWeb/app/img/botones/objetoCosto");
					} else {
						allrecords[i].setAttribute("tipo", "/SASWeb/app/img/botones/actividad");
					}
				
				Log.info("dataArrived: "+  allrecords[i].getAttribute("tipoModulo"));
				}
			}
		});
		
		setFields(tipoField, nameField, refField, dqfField, driverField);
	}

	public ListGridField getDqfField() {
		return dqfField;
	}

	protected void refreshData(String display, String modelo) {

		Log.info("Refresh data AsignacionesView");

		Criteria crit = new Criteria("referencia", display);
		crit.addCriteria("modelo", modelo);

		fetchData(crit);
	}

}
