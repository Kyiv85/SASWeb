package gwt.adminsas.client;

import java.util.HashMap;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.DataArrivedEvent;
import com.smartgwt.client.widgets.form.fields.events.DataArrivedHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripSeparator;
import com.smartgwt.client.widgets.tree.Tree;

/**
 * Barra de herramientas primaria de SASWebAdmin
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class MainToolbar extends ToolStrip {

	protected ImgButton recurso = new ImgButton();
	protected ImgButton actividad = new ImgButton();
	protected ImgButton objetoCosto = new ImgButton();

	protected IButton multiNivel = new IButton();
	protected IButton voyPara = new IButton();

	protected IButton expandir = new IButton();
	protected IButton contraer = new IButton();
	protected IButton expandirMultiple = new IButton();
	protected IButton contraerMultiple = new IButton();

	protected IButton dim1Button = new IButton();
	protected IButton dim2Button = new IButton();
	protected IButton dim3Button = new IButton();
	protected IButton dim4Button = new IButton();
	protected IButton dim5Button = new IButton();
	protected IButton dim6Button = new IButton();

	private MultiDimensionGrid multiDim;
	private DataFilterToolBar dataToolBar;

	public void setMultiView(MultiDimensionGrid multiDimensionV) {

		this.multiDim = multiDimensionV;
	}

	/**
	 * Constructor por defecto
	 */
	protected MainToolbar(DataFilterToolBar dataToolbar) {

		ToolStripSeparator stripSeparator = new ToolStripSeparator();
		stripSeparator.setHeight(20);

		setWidth("*");
		setHeight(24);

		this.dataToolBar = dataToolbar;
		// Copiar
		IButton copiar = new IButton();
		copiar.setIcon("/SASWeb/app/img/botones/copy.png");
		copiar.setIconAlign("left");
		copiar.setIconWidth(16);
		copiar.setIconHeight(16);
		copiar.setSize("23px", "19px");

		addMember(copiar);
		addMember(stripSeparator);

		// Dimensiones
		dim1Button.setIcon("/SASWeb/app/img/botones/DIM1.png");
		dim1Button.setIconAlign("left");
		dim1Button.setIconWidth(16);
		dim1Button.setIconHeight(16);
		dim1Button.setSize("23px", "19px");
		dim1Button.setDisabled(false);

		addMember(dim1Button);

		dim2Button.setIcon("/SASWeb/app/img/botones/DIM2.png");
		dim2Button.setIconAlign("left");
		dim2Button.setIconWidth(16);
		dim2Button.setIconHeight(16);
		dim2Button.setSize("23px", "19px");
		dim2Button.setDisabled(true);
		addMember(dim2Button);

		dim3Button.setIcon("/SASWeb/app/img/botones/DIM3.png");
		dim3Button.setIconAlign("left");
		dim3Button.setIconWidth(16);
		dim3Button.setIconHeight(16);
		dim3Button.setSize("23px", "19px");
		dim3Button.setDisabled(true);
		addMember(dim3Button);

		dim4Button.setIcon("/SASWeb/app/img/botones/DIM4.png");
		dim4Button.setIconAlign("left");
		dim4Button.setIconWidth(16);
		dim4Button.setIconHeight(16);
		dim4Button.setSize("23px", "19px");
		dim4Button.setDisabled(true);
		addMember(dim4Button);

		dim5Button.setIcon("/SASWeb/app/img/botones/DIM5.png");
		dim5Button.setIconAlign("left");
		dim5Button.setIconWidth(16);
		dim5Button.setIconHeight(16);
		dim5Button.setSize("23px", "19px");
		dim5Button.setDisabled(true);
		addMember(dim5Button);

		dim6Button.setIcon("/SASWeb/app/img/botones/DIM6.png");
		dim6Button.setIconAlign("left");
		dim6Button.setIconWidth(16);
		dim6Button.setIconHeight(16);
		dim6Button.setSize("23px", "19px");
		dim6Button.setDisabled(true);
		addMember(dim6Button);

		addMember(stripSeparator);

		// Vista MultiNivel
		multiNivel.setIcon("/SASWeb/app/img/botones/multiNivel_selected.png");
		multiNivel.setIconAlign("left");
		multiNivel.setActionType(SelectionType.RADIO);
		multiNivel.setRadioGroup("vista");
		multiNivel.setSelected(true);

		multiNivel.setIconWidth(16);
		multiNivel.setIconHeight(16);
		multiNivel.setSize("23px", "19px");
		addMember(multiNivel);

		// Vista voyPara
		voyPara.setIcon("/SASWeb/app/img/botones/paraDondeVoy_disabled.png");
		voyPara.setIconAlign("left");
		voyPara.setActionType(SelectionType.RADIO);
		voyPara.setRadioGroup("vista");
		voyPara.setIconWidth(16);
		voyPara.setIconHeight(16);
		voyPara.setSize("23px", "19px");
		voyPara.setSelected(false);

		addMember(voyPara);
		addMember(stripSeparator);

		// Filtro 'Recurso'
		recurso.setSize(24);
		recurso.setShowRollOver(false);
		recurso.setSrc("/SASWeb/app/img/botones/recurso.png");
		recurso.setActionType(SelectionType.RADIO);
		recurso.setRadioGroup("textAlign");
		recurso.setSelected(true);

		recurso.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				String periodoScenario = (String) dataToolBar.periodItems
						.getValue();

				Record currentSelection = dataToolBar.modelsItem
						.getSelectedRecord();
				String modelo = currentSelection.getAttribute("modelID");
				
				multiDim.clearAllGrids();
				
				multiDim.icons = new HashMap<String, String>();

				multiDim.iconIndex = 0;
				multiDim.openStateTree = "";

				dataToolBar.asignacion.setDisabled(true);
				dataToolBar.eliminarAsignacion.setDisabled(true);
				
				dataToolBar.setCurrentModel(currentSelection.getAttribute("model"));
				dataToolBar.setCurrentModule("RESOURCE");
				dataToolBar.refreshTitle();
				
				dataToolBar.eliminarAsignacion.setDisabled(true);
				
				multiDim.refreshData(modelo, periodoScenario, "RESOURCE");

			}

		});

		addMember(recurso);

		// Filtro 'Actividad'
		actividad.setSize(24);
		actividad.setShowRollOver(false);
		actividad.setSrc("/SASWeb/app/img/botones/actividad.png");
		actividad.setActionType(SelectionType.RADIO);
		actividad.setRadioGroup("textAlign");

		actividad.setSelected(false);

		actividad.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				String periodoScenario = (String) dataToolBar.periodItems
						.getValue();

				Record currentSelection = dataToolBar.modelsItem
						.getSelectedRecord();
				String modelo = currentSelection.getAttribute("modelID");

				multiDim.clearAllGrids();

				multiDim.icons = new HashMap<String, String>();
				multiDim.iconIndex = 0;
				multiDim.openStateTree = "";

				dataToolBar.asignacion.setDisabled(true);
				dataToolBar.eliminarAsignacion.setDisabled(true);

				dataToolBar.setCurrentModel(currentSelection.getAttribute("model"));
				dataToolBar.setCurrentModule("ACTIVITY");
				dataToolBar.refreshTitle();
				
				multiDim.refreshData(modelo, periodoScenario, "ACTIVITY");
			}
		});

		addMember(actividad);

		// Filtro 'ObjetoCosto'
		objetoCosto.setSize(24);
		objetoCosto.setShowRollOver(false);
		objetoCosto.setSrc("/SASWeb/app/img/botones/objetoCosto.png");
		objetoCosto.setActionType(SelectionType.RADIO);
		objetoCosto.setRadioGroup("textAlign");

		objetoCosto.setSelected(false);

		objetoCosto.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {

				String periodoScenario = (String) dataToolBar.periodItems
						.getValue();

				Record currentSelection = dataToolBar.modelsItem
						.getSelectedRecord();
				String modelo = currentSelection.getAttribute("modelID");

				multiDim.clearAllGrids();

				multiDim.icons = new HashMap<String, String>();
				multiDim.iconIndex = 0;
				multiDim.openStateTree = "";

				dataToolBar.asignacion.setDisabled(true);
				dataToolBar.eliminarAsignacion.setDisabled(true);

				dataToolBar.setCurrentModel(currentSelection.getAttribute("model"));
				dataToolBar.setCurrentModule("COSTOBJECT");
				dataToolBar.refreshTitle();
				
				multiDim.refreshData(modelo, periodoScenario, "COSTOBJECT");
			}
		});

		// Evento para cambio de Modelo (Despues)
		dataToolBar.modelsItem.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {

				dataToolBar.periodItems.clearValue();
			}
		});

		dataToolBar.periodItems.addDataArrivedHandler(new DataArrivedHandler() {
			@Override
			public void onDataArrived(DataArrivedEvent event) {

				String module = "";

				Record currentSelection = dataToolBar.modelsItem
						.getSelectedRecord();
				try {
					String modelo = currentSelection.getAttribute("modelID");

					String periodoScenario = (String) dataToolBar.periodItems
							.getValue();

					if (recurso.isSelected())
						module = "RESOURCE";
					else if (actividad.isSelected())
						module = "ACTIVITY";
					else if (objetoCosto.isSelected())
						module = "COSTOBJECT";

					//Tree tree = multiDim.getTree();
					//tree.removeList(tree.getAllNodes());

					multiDim.clearAllGrids();
					
					multiDim.icons = new HashMap<String, String>();

					multiDim.iconIndex = 0;
					multiDim.openStateTree = "";

					// Se filtra la vista del arbol con el modelo dado
					multiDim.refreshData(modelo, periodoScenario, module);
					
					dataToolBar.setCurrentModel(currentSelection.getAttribute("model"));
					dataToolBar.setCurrentModule("RESOURCE");
					dataToolBar.refreshTitle();
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		// Evento para cambio de Periodo/Escenario
		dataToolBar.periodItems.addChangedHandler(new ChangedHandler() {
			public void onChanged(ChangedEvent event) {

				String module = "";

				String periodoScenario = (String) dataToolBar.periodItems
						.getValue();

				Record currentSelection = dataToolBar.modelsItem
						.getSelectedRecord();
				String modelo = currentSelection.getAttribute("modelID");

				if (recurso.isSelected())
					module = "RESOURCE";
				else if (actividad.isSelected())
					module = "ACTIVITY";
				else if (objetoCosto.isSelected())
					module = "COSTOBJECT";

				//Tree tree = multiDim.getTree();
				//tree.removeList(tree.getAllNodes());

				multiDim.clearAllGrids();
				
				multiDim.icons = new HashMap<String, String>();

				multiDim.iconIndex = 0;
				multiDim.openStateTree = "";

				// Se filtra la vista del arbol con el modelo dado
				multiDim.refreshData(modelo, periodoScenario, module);
			}
		});

		addMember(objetoCosto);
		addMember(stripSeparator);

		// Manipulación de Arbol MultiDimension
		// Expandir
		expandir.setIcon("/SASWeb/app/img/botones/expandir.png");
		expandir.setIconAlign("left");
		expandir.setIconWidth(16);
		expandir.setIconHeight(16);
		expandir.setSize("21px", "19px");
		addMember(expandir);

		// Contraer
		contraer.setIcon("/SASWeb/app/img/botones/contraer.png");
		contraer.setIconAlign("left");
		contraer.setIconWidth(16);
		contraer.setIconHeight(16);
		contraer.setSize("21px", "19px");
		addMember(contraer);

		// Expandir Multiples
		expandirMultiple
				.setIcon("/SASWeb/app/img/botones/expandirMultiple.png");
		expandirMultiple.setIconAlign("left");
		expandirMultiple.setIconWidth(16);
		expandirMultiple.setIconHeight(16);
		expandirMultiple.setSize("21px", "19px");
		addMember(expandirMultiple);

		// Contraer Multiples
		contraerMultiple
				.setIcon("/SASWeb/app/img/botones/contraerMultiple.png");
		contraerMultiple.setIconAlign("left");
		contraerMultiple.setIconWidth(16);
		contraerMultiple.setIconHeight(16);
		contraerMultiple.setSize("21px", "19px");
		addMember(contraerMultiple);

		draw();
	}
}
