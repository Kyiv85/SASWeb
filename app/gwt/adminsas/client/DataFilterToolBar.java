package gwt.adminsas.client;

import gwt.adminsas.client.data.ModelsDS;
import gwt.adminsas.client.data.PeriodoEscenarioDS;

import com.google.gwt.user.client.Window;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.WidgetCanvas;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripSeparator;

/**
 * Barra de herramientas secundaria de SASWebAdmin
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class DataFilterToolBar extends ToolStrip {

	protected SelectItem modelsItem;
	protected SelectItem periodItems;

	protected IButton asignacion = new IButton("");
	protected IButton eliminarAsignacion = new IButton("");

	private String currentModule = "";
	private String currentModel = "";

	private DynamicForm modelsForm;
	private DynamicForm periodForm;
	private MultiDimensionGrid multiDim;
	private TitleBar barTitle;

	/**
	 * Constructor por defecto
	 */
	public DataFilterToolBar() {

		// Inicialización del la barra de herramientas
		setWidth("*");
		setHeight(24);

		// Inicializacion form de Modelos
		modelsForm = new DynamicForm();
		modelsForm.setShowResizeBar(true);
		modelsForm.setWidth("180");
		modelsForm.setMinWidth(50);
		modelsForm.setNumCols(1);

		Label modelLabel = new Label();
		modelLabel.setWrap(false);
		modelLabel.setAlign(Alignment.CENTER);
		modelLabel.setValign(VerticalAlignment.CENTER);
		modelLabel.setContents("<b>Modelos:</b>");

		ToolStripSeparator stripSeparator = new ToolStripSeparator();
		stripSeparator.setHeight(8);

		modelsItem = new SelectItem();

		modelsItem.setShowTitle(false);
		modelsItem.setWidth("*");

		DataSource modelDS = ModelsDS.getInstance();
		modelsItem.setOptionDataSource(modelDS);
		modelsItem.setAutoFetchData(true);
		modelsItem.setDefaultToFirstOption(true);

		modelsForm.setItems(modelsItem);

		// Inicializacion de form de Periodo/Escenario
		periodForm = new DynamicForm();
		periodForm.setWidth("180");
		periodForm.setMinWidth(50);
		periodForm.setNumCols(1);

		Label periodLabel = new Label();
		periodLabel.setWrap(false);
		periodLabel.setAlign(Alignment.CENTER);
		periodLabel.setValign(VerticalAlignment.CENTER);
		periodLabel.setContents("<b>Periodo/Escenario:</b>");

		periodItems = new SelectItem() {
			// lista de periodos luego de seleccionar un modelo
			protected Criteria getPickListFilterCriteria() {

				Record currentSelection = modelsItem.getSelectedRecord();

				String modeloID = (String) modelsItem.getValue();
				String displayValue = currentSelection == null ? ""
						: currentSelection.getAttribute("modelID");

				Criteria criteria = new Criteria("modelo", displayValue);
				criteria.addCriteria(new Criteria("modeloID", modeloID));

				return criteria;
			}
		};

		periodItems.setDefaultToFirstOption(true);

		periodItems.setName("selectName");
		periodItems.setShowTitle(false);
		periodItems.setWidth("180");
		periodItems.setPickListWidth(250);

		DataSource periodScenarioDS = PeriodoEscenarioDS.getInstance();

		periodItems.setDisplayField("periodoScenario");
		periodItems.setValueField("periodoScenarioID");
		periodItems.setOptionDataSource(periodScenarioDS);

		periodForm.setItems(periodItems);

		// Inicializacion de botones para la edición de Asignaciones
		// de cada "hamburguesa"
		asignacion.setIcon("/SASWeb/app/img/botones/anadirAsignacion.png");
		asignacion.setShowRollOver(false);
		asignacion.setShowDown(false);
		asignacion.setIconAlign("left");
		asignacion.setIconWidth(16);
		asignacion.setIconHeight(16);
		asignacion.setSize("23px", "19px");

		asignacion.setDisabled(true);

		eliminarAsignacion
				.setIcon("/SASWeb/app/img/botones/eliminarAsignacion.png");
		eliminarAsignacion.setShowRollOver(false);
		eliminarAsignacion.setShowDown(false);
		eliminarAsignacion.setIconAlign("left");
		eliminarAsignacion.setIconWidth(16);
		eliminarAsignacion.setIconHeight(16);
		eliminarAsignacion.setSize("23px", "19px");
		eliminarAsignacion.setDisabled(true);

		LinkItem linkItem = new LinkItem();
		linkItem.setLinkTitle("Salir");
	 	linkItem.setTarget("_self");
	 	linkItem.setShowTitle(false);

		DynamicForm logoutForm = new DynamicForm();
		logoutForm.setAlign(Alignment.RIGHT);
		logoutForm.setItems(linkItem);
		linkItem.setAlign(Alignment.RIGHT);
		
		linkItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
		
				Window.open("/SASWeb/logout", "_self", "");
			}
		});
		
		addMember(stripSeparator);

		addMember(asignacion);
		addMember(eliminarAsignacion);
		addMember(stripSeparator);

		addMember(modelLabel);
		addMember(modelsForm);

		addMember(stripSeparator);

		addMember(periodLabel);
		
		addMember(periodForm);

        ToolStripSeparator bigStrip = new ToolStripSeparator();
		bigStrip.setWidth100();
		bigStrip.setHeight(0);
		
		addMember(bigStrip);
		
		addMember(logoutForm);
		
		draw();
	}

	public void setTitlebar(TitleBar adminTitle) {

		this.barTitle = adminTitle;

		// Evento para cambio de Modelo (Antes)
		modelsItem.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {

				// Se vacía el dropdown de Periodo/Escenario
				String title = "<b>Modulo " + currentModule + "  : Modelo "
						+ currentModel + "</b>";

				barTitle.titleItem.setContents(title);
			}
		});
	}

	public void refreshTitle() {

		// Se vacía el dropdown de Periodo/Escenario
		String title = "<b>Modulo " + currentModule + "  : Modelo " + currentModel
				+ "</b>";
		barTitle.titleItem.setContents(title);
	}

	public String getCurrentModel() {
		return currentModel;
	}

	public void setCurrentModel(String currentModel) {
		this.currentModel = currentModel;
	}

	public String getCurrentModule() {
		return currentModule;
	}

	public void setCurrentModule(String currentModule) {
		this.currentModule = currentModule;
	}

	public void setMultiView(MultiDimensionGrid multiDimensionV) {

		this.multiDim = multiDimensionV;

		modelsItem
				.addDataArrivedHandler(new com.smartgwt.client.widgets.form.fields.events.DataArrivedHandler() {
					@Override
					public void onDataArrived(
							com.smartgwt.client.widgets.form.fields.events.DataArrivedEvent event) {

						Record record = event.getData().get(0);
						multiDim.setCurrentModel(record.getAttribute("modelID"));
					}
				});

	}

}
