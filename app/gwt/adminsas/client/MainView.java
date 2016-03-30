package gwt.adminsas.client;

import gwt.adminsas.client.data.ConductorDS;
import gwt.adminsas.client.data.MultiDimensionDS;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.user.client.History;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * Vista Principal de la aplicación SASWebAdmin
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class MainView extends Canvas {

	private final String initToken = History.getToken();

	private MultiDimensionGrid multiDimView;
	private AsignacionesView asigView;

	private VLayout mainLayout;

	private TitleBar adminTitle = new TitleBar();

	private DataFilterToolBar dataToolbar;
	private MainToolbar mainToolbar;

	private HLayout dataToolBarLayout = new HLayout();
	private HLayout mainToolBarLayout = new HLayout();

	private VLayout multiViewLayout = new VLayout();
	private VLayout asignacionesLayout = new VLayout();
	private HLayout multihLayout = new HLayout();

	private IButton recurso = new IButton("Recurso");
	private IButton actividad = new IButton("Actividad");
	private IButton objetoCosto = new IButton("Objeto de Costo");

	private AsignacionesDestinoGrid asignacionesTree = new AsignacionesDestinoGrid();

	private static int nodePosition = 0;

	enum Dimensiones {
		DIM1, DIM2, DIM3, DIM4, DIM5, DIM6, DIM7;
	}

	/**
	 * Constructor por defecto
	 * 
	 */
	public MainView() {
		mainLayout = new VLayout() {
			protected void onInit() {
				super.onInit();
				if (initToken.length() != 0) {
					onHistoryChanged(initToken);
				}
			}

			private void onHistoryChanged(String initToken) {
			}
		};
		showmainView();
	}

	/**
	 * Inicialización de elementos de SASWebAdmin
	 * 
	 */
	private void showmainView() {

		// Configuración de tamaños y margenes de interfaz
		mainLayout.setWidth100();
		mainLayout.setHeight100();
		mainLayout.setLayoutMargin(2);

		multihLayout.setWidth100();
		multihLayout.setHeight100();

		// Titulo
		HLayout titleLayout = new HLayout();
		titleLayout.setWidth100();
		titleLayout.setHeight(50);
		titleLayout.addMember(adminTitle);
		titleLayout.setMargin(2);

		// Vista de Asignaciones
		dataToolbar = new DataFilterToolBar();
		asigView = new AsignacionesView();

		mainToolbar = new MainToolbar(dataToolbar);
		multiDimView = new MultiDimensionGrid(asigView, dataToolbar,
				mainToolbar);

		mainToolbar.setMultiView(multiDimView);
		dataToolbar.setTitlebar(adminTitle);
		dataToolbar.setMultiView(multiDimView);

		// Toolbar Superior
		mainToolBarLayout.setWidth100();
		mainToolBarLayout.addMember(mainToolbar);
		mainToolBarLayout.setMargin(2);

		// Evento de cambio a vista MultiNivel
		mainToolbar.multiNivel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				mainToolbar.multiNivel
						.setIcon("/SASWeb/app/img/botones/multiNivel.png");
				mainToolbar.voyPara
						.setIcon("/SASWeb/app/img/botones/paraDondeVoy_disabled.png");
				multihLayout.removeMember(multiViewLayout);
				multihLayout.removeMember(asignacionesLayout);

				multihLayout.addMember(multiViewLayout);

				multiViewLayout.setShowResizeBar(false);

				multiViewLayout.setHeight100();
				multiViewLayout.setWidth100();

				multihLayout.redraw();
			}
		});

		// Evento de cambio a vista Voy Para
		mainToolbar.voyPara.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				mainToolbar.multiNivel
						.setIcon("/SASWeb/app/img/botones/multiNivel_disabled.png");
				mainToolbar.voyPara
						.setIcon("/SASWeb/app/img/botones/paraDondeVoy.png");

				multihLayout.removeMember(multiViewLayout);
				multihLayout.removeMember(asignacionesLayout);

				multihLayout.addMember(multiViewLayout);
				multihLayout.addMember(asignacionesLayout);

				multiViewLayout.setShowResizeBar(true);

				multihLayout.redraw();

			}
		});

		// Inicializar eventos de inserción de nodos, por dimensiones
		mainToolbar.dim1Button.addClickHandler(new DimensionClickHandler(0));
		mainToolbar.dim2Button.addClickHandler(new DimensionClickHandler(1));
		mainToolbar.dim3Button.addClickHandler(new DimensionClickHandler(2));

		mainToolbar.dim4Button.addClickHandler(new DimensionClickHandler(3));
		mainToolbar.dim5Button.addClickHandler(new DimensionClickHandler(4));
		mainToolbar.dim6Button.addClickHandler(new DimensionClickHandler(5));

		// Evento expansión de arbol. Boton 1
		mainToolbar.expandir.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				Tree tree = multiDimView.getData();
				TreeNode node = (TreeNode) multiDimView.getSelectedRecord();
				tree.openFolder(node);
				multiDimView.setOpenStateTree(multiDimView.getOpenState());
			}
		});

		// Evento contraer nodo. Boton 2
		mainToolbar.contraer.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				Tree tree = multiDimView.getData();
				TreeNode node = (TreeNode) multiDimView.getSelectedRecord();
				tree.closeFolder(node);
				multiDimView.setOpenStateTree(multiDimView.getOpenState());
			}
		});

		// Evento expansión de todos los nodos del arbol. Boton 3
		mainToolbar.expandirMultiple.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				Map<String, Object> map = new HashMap<String, Object>();
				
				Tree tree = multiDimView.getData();
				TreeNode node = (TreeNode) multiDimView.getSelectedRecord();
				 
				int currentLevel = tree.getLevel(node); 
				TreeNode[] descendants = tree.getDescendantFolders(node);
				
				if(!tree.isOpen(node)){
					
					map.put("dimActual", node.getAttribute("DimName"));
					multiDimView.getDataSource().setDefaultParams(map);
						
					multiDimView.getData().openFolder(node);
					
				}else{
				
					for (int i = 0; i < descendants.length; i++) {
						
						// si descendientes son 3 niveles mayores al parent
						// y no está cargado
						if ((tree.getLevel(descendants[i]) > currentLevel + 2) 
								&&  !tree.isLoaded(descendants[i]) ) {
							event.cancel();
							return;
						}
						
						// Abrimos descendientes si no están abiertos
						if(!tree.isOpen(descendants[i])){
							
							map.put("dimActual", descendants[i].getAttribute("DimName"));
 							multiDimView.getDataSource().setDefaultParams(map);
 							
							tree.openFolder(descendants[i]);
						}
					}
				}
				multiDimView.setOpenStateTree(multiDimView.getOpenState());
			}
		});

		// Evento contraer todos los nodos del arbol. Boton 4
		mainToolbar.contraerMultiple.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				
				Tree tree = multiDimView.getData();
				TreeNode node = (TreeNode) multiDimView.getSelectedRecord();
				tree.closeAll(node);
				multiDimView.setOpenStateTree(multiDimView.getOpenState());

			}
		});

		// Toolbar Secundario
		dataToolBarLayout.setWidth100();
		dataToolBarLayout.addMember(dataToolbar);
		dataToolBarLayout.setMargin(2);

		recurso.setShowRollOver(false);
		recurso.setIcon("/SASWeb/app/img/botones/recurso.png");
		recurso.setActionType(SelectionType.RADIO);
		recurso.setRadioGroup("assignment");
		recurso.setWidth(140);

		recurso.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				String periodoScenario = (String) dataToolbar.periodItems
						.getValue();

				Record currentSelection = dataToolbar.modelsItem
						.getSelectedRecord();
				String modelo = currentSelection.getAttribute("modelID");

				asignacionesTree.icons = new HashMap<String, String>();
				asignacionesTree.iconIndex = 0;

				asignacionesTree.refreshData(modelo, periodoScenario,
						"RESOURCE");
			}
		});

		// Filtro 'Actividad'
		actividad.setShowRollOver(false);
		actividad.setIcon("/SASWeb/app/img/botones/actividad.png");
		actividad.setActionType(SelectionType.RADIO);
		actividad.setRadioGroup("assignment");
		actividad.setWidth(140);

		actividad.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				String periodoScenario = (String) dataToolbar.periodItems
						.getValue();

				Record currentSelection = dataToolbar.modelsItem
						.getSelectedRecord();
				String modelo = currentSelection.getAttribute("modelID");

				asignacionesTree.icons = new HashMap<String, String>();
				asignacionesTree.iconIndex = 0;

				asignacionesTree.refreshData(modelo, periodoScenario,
						"ACTIVITY");
			}
		});

		// Filtro 'ObjetoCosto'
		objetoCosto.setShowRollOver(false);
		objetoCosto.setIcon("/SASWeb/app/img/botones/objetoCosto.png");
		objetoCosto.setActionType(SelectionType.RADIO);
		objetoCosto.setRadioGroup("assignment");
		objetoCosto.setWidth(140);

		objetoCosto.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				String periodoScenario = (String) dataToolbar.periodItems
						.getValue();

				Record currentSelection = dataToolbar.modelsItem
						.getSelectedRecord();
				String modelo = currentSelection.getAttribute("modelID");

				asignacionesTree.icons = new HashMap<String, String>();
				asignacionesTree.iconIndex = 0;

				asignacionesTree.refreshData(modelo, periodoScenario,
						"COSTOBJECT");
			}
		});

		// Evento Agregar cuenta para asignaciones
		dataToolbar.asignacion.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				showAsignacionDialog();
			}
		});

		// Evento eliminar cuenta para asignaciones
		dataToolbar.eliminarAsignacion.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				final ListGridRecord node = asigView.getSelectedRecord();

				SC.confirm(
						"Está seguro que desea borrar "
								+ node.getAttribute("nombre") + "?",
						new BooleanCallback() {
							public void execute(Boolean value) {
								if (value != null && value) {

									Map<String, Object> map = new HashMap<String, Object>();

									TreeNode origenRecord = (TreeNode) multiDimView
											.getSelectedRecord();
									ListGridRecord destinoRecord = asigView
											.getSelectedRecord();

									map.put("periodo",
											multiDimView.periodoActual);
									map.put("modelo",
											origenRecord.getAttribute("Modelo"));

									map.put("modulofuente", origenRecord
											.getAttribute("tipoModulo"));
									map.put("dimensionfuente",
											origenRecord.getAttribute("DisplayRef"));

									map.put("modulodestino", destinoRecord
											.getAttribute("tipoModulo"));
									map.put("dimensiondestino", destinoRecord
											.getAttribute("referencia"));

									asigView.getDataSource().setDefaultParams(
											map);

									asigView.removeSelectedData();
								}
							}
						});
			}
		});

		// Configuración de tamaños y margenes de Multiview
		multiViewLayout.setHeight100();
		multiViewLayout.setWidth100();
		multiViewLayout.setShowResizeBar(false);

		// Layout de asignaciones
		asignacionesLayout.setHeight("100%");
		asignacionesLayout.setWidth("75%");

		asignacionesLayout.addMember(asigView);
		multiViewLayout.addMember(multiDimView);

		multihLayout.addMember(multiViewLayout);
		mainLayout.addMember(titleLayout);
		mainLayout.addMember(mainToolBarLayout);
		mainLayout.addMember(dataToolBarLayout);
		mainLayout.addMember(multihLayout);
		
		mainLayout.draw();
	}

	
	/**
	 * Ventana de inserción de asignaciones
	 * 
	 */
	private void showAsignacionDialog() {

		final Window asignacionesWindow = new Window();
	
		final IButton agregarCuenta = new IButton("");
		agregarCuenta.setTitle("Agregar Cuenta");
		agregarCuenta.setWidth(140);
		agregarCuenta.setDisabled(true);

		IButton closeCuenta = new IButton("");
		
		// Dialogo para seleccionar cuenta destino
		asignacionesWindow.setWidth(540);
		asignacionesWindow.setHeight(420);
		asignacionesWindow.setAlign(Alignment.CENTER);

		asignacionesWindow.setTitle("Agregar Asignaciones");
		asignacionesWindow.setShowMinimizeButton(false);
		asignacionesWindow.setIsModal(true);
		asignacionesWindow.setShowModalMask(false);
		asignacionesWindow.centerInPage();

		VLayout topWindow = new VLayout(10);
		topWindow.setAlign(Alignment.CENTER);
		topWindow.setWidth100();

		// Evento de selección de registros en el arbol de asignaciones
		asignacionesTree.addRecordClickHandler(new RecordClickHandler() {
			@Override
			public void onRecordClick(RecordClickEvent event) {

				// Obtener el nodo seleccionado
				Record node = event.getRecord();

				if (node.getAttribute("isLeaf") != null) {
					agregarCuenta.setDisabled(false);
				} else {
					agregarCuenta.setDisabled(true);
				}
			}
		});

		asignacionesWindow.addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				asignacionesWindow.destroy();
			}
		});

		closeCuenta.setTitle("Cerrar");
		closeCuenta.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				asignacionesWindow.destroy();
			}
		});
		
		// Selector de Conductores, basado en modelo actual
		final SelectItem driversItem = new SelectItem() {
			// lista de periodos luego de seleccionar un modelo
			protected Criteria getPickListFilterCriteria() {

				TreeNode multiDimNode = (TreeNode) multiDimView
						.getSelectedRecord();

				Criteria crit = new Criteria();
				crit.addCriteria("modelo", multiDimNode.getAttribute("Modelo"));
				crit.addCriteria("periodo",
						multiDimNode.getAttribute("IDPeriodo"));
				crit.addCriteria("escenario",
						multiDimNode.getAttribute("IDEscenario"));

				return crit;
			}
		};

		// Estado de botones de selección de modulo
		if (mainToolbar.recurso.isSelected()) {

			recurso.setDisabled(false);
			actividad.setDisabled(false);
			objetoCosto.setDisabled(false);
		} else if (mainToolbar.actividad.isSelected()) {

			recurso.setDisabled(true);
			actividad.setDisabled(false);
			objetoCosto.setDisabled(false);
		} else if (mainToolbar.objetoCosto.isSelected()) {

			recurso.setDisabled(true);
			actividad.setDisabled(true);
			objetoCosto.setDisabled(false);
		}

		final ConductorDS conductorData = ConductorDS.getInstance();
		conductorData.invalidateCache();
		driversItem.setShowTitle(false);
		driversItem.setOptionDataSource(conductorData);
		driversItem.setAutoFetchData(true);

		// Form combobox
		DynamicForm driversForm = new DynamicForm();
		driversForm.setWidth100();

		driversForm.setItems(driversItem);

		// Layout de panel superior
		VStack modules = new VStack(15);
		modules.setWidth("40%");
		modules.setHeight("40%");
		modules.setAlign(Alignment.CENTER);

		modules.addMember(recurso, 1);
		modules.addMember(actividad, 2);
		modules.addMember(objetoCosto, 3);

		Label modulesLabel = new Label();
		modulesLabel.setWrap(false);
		modulesLabel.setAlign(Alignment.CENTER);
		modulesLabel.setValign(VerticalAlignment.CENTER);
		modulesLabel.setContents("<b>Modulos:</b>");

		HStack modulesLayout = new HStack();
		modulesLayout.setAlign(Alignment.CENTER);
		modulesLayout.setWidth100();
		modulesLayout.addMember(modulesLabel);
		modulesLayout.addMember(modules);

		Label driversLabel = new Label();
		driversLabel.setWrap(false);
		driversLabel.setAlign(Alignment.CENTER);
		driversLabel.setContents("<b>Conductores:</b>");
		driversLabel.setHeight("40%");

		VStack driversFormLayout = new VStack();
		driversFormLayout.setWidth("40%");
		driversFormLayout.setHeight("40%");
		driversFormLayout.setAlign(VerticalAlignment.BOTTOM);

		driversFormLayout.addMember(driversForm);

		HStack driversLayout = new HStack();
		driversLayout.setAlign(Alignment.CENTER);
		driversLayout.setWidth100();
		driversLayout.addMember(driversLabel);
		driversLayout.addMember(driversFormLayout);

		topWindow.addMember(modulesLayout);
		topWindow.addMember(driversLayout);

		HLayout buttons = new HLayout(15);
		buttons.addMember(agregarCuenta);
		buttons.addMember(closeCuenta);
		buttons.setAlign(Alignment.CENTER);
		buttons.setWidth100();
		buttons.setHeight(35);

		VLayout grid = new VLayout();
		grid.addMember(asignacionesTree);

		agregarCuenta.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				TreeNode newAsignacion = new TreeNode();
				final TreeNode origenRecord = (TreeNode) multiDimView
						.getSelectedRecord();
				final TreeNode destinoRecord = (TreeNode) asignacionesTree
						.getSelectedRecord();

				Record curDriver = driversItem.getSelectedRecord();

				if (curDriver == null) {
					SC.warn("Debe seleccionar un conductor");
					event.cancel();
					return;
				}else
					if(origenRecord.getAttribute("DisplayRef").
						equalsIgnoreCase(destinoRecord.getAttribute("DisplayRef"))){
					
					SC.warn("No se permiten asignaciones ciclicas");
					event.cancel();
					return;
					
				}

				newAsignacion.setAttribute("drivername",
						curDriver.getAttribute("conductorID"));

				// Insertamos nuevo nodo con data por defecto
				// General
				newAsignacion.setAttribute("modelo",
						origenRecord.getAttribute("Modelo"));
				newAsignacion.setAttribute("periodo",
						origenRecord.getAttribute("IDPeriodo"));
				newAsignacion.setAttribute("escenario",
						origenRecord.getAttribute("IDEscenario"));
				newAsignacion.setAttribute("DQF", "0");

				// Origen
				newAsignacion.setAttribute("dimensionfuente",
						origenRecord.getAttribute("DimName"));
				newAsignacion.setAttribute("dimfuenteref",
						origenRecord.getAttribute("DisplayRef"));
				newAsignacion.setAttribute("modulofuente",
						origenRecord.getAttribute("tipoModulo"));

				// Destino
				newAsignacion.setAttribute("dimensiondestino",
						destinoRecord.getAttribute("DimName"));
				newAsignacion.setAttribute("dimdestinoref",
						destinoRecord.getAttribute("DisplayRef"));
				newAsignacion.setAttribute("modulodestino",
						destinoRecord.getAttribute("tipoModulo"));

				asigView.getDataSource().addData(newAsignacion,
						new DSCallback() {
							@Override
							public void execute(DSResponse response,
									Object rawData, DSRequest request) {

								if (response.getStatus() >= 0) {

									String disp = origenRecord.getAttribute("DisplayRef");
									String model = origenRecord.getAttribute("Modelo");

									Criteria crit = new Criteria("referencia",disp);
									crit.addCriteria("modelo", model);
									asigView.invalidateCache();
									asigView.fetchData(crit);

									// asignacionesWindow.destroy();
								} else {
									SC.warn("No se pudo insertar este registro en asignaciones.");
								}
							}
						});
			}
		});

		asignacionesWindow.addItem(topWindow);
		asignacionesWindow.addItem(grid);
		asignacionesWindow.addItem(buttons);

		asignacionesWindow.show();

		conductorData.invalidateCache();
		driversItem.fetchData();
	}

	
	/**
	 * 
	 * Clase interna que implementa metodo de agregar nodos en la Multivista
	 * 
	 */
	private class DimensionClickHandler implements ClickHandler {

		private int index;

		public DimensionClickHandler(int index) {
			this.index = index;
		}

		// Dirige metodo de como agregar nodos al arbol
		@Override
		public void onClick(ClickEvent event) {

			MultiDimensionDS ds = (MultiDimensionDS) multiDimView
					.getDataSource();
			JSONArray jerarquia = ds.getJerarquia();
			int newLevel;

			TreeNode node = new TreeNode();

			TreeNode parent = (TreeNode) multiDimView.getSelectedRecord();
			Tree tree = multiDimView.getData();

			if (parent == null) {

				SC.warn("Debe seleccionar un elemento en el Arbol");
				return;
			} else {
				tree.openFolder(parent);
				multiDimView.setOpenStateTree(multiDimView.getOpenState());

				if (parent.getAttribute("isLeaf") != null) {

					parent.setAttribute("isLeaf", (Object) null);
					multiDimView.setIconAndLevel(parent);

					// Parte de los atributos del nuevo nodo son copiados 
					// del padre
					node.setAttribute("isLeaf", "true");
					node.setAttribute("UltDimension",
							parent.getAttribute("UltDimension"));
					node.setAttribute("DimName", parent.getAttribute("DimName"));
					node.setAttribute("tipoModulo",
							parent.getAttribute("tipoModulo"));

					multiDimView.setIconAndLevel(node);
				}
			}

			// Insertamos nuevo nodo con data por defecto
			node.setAttribute("ID", jerarquia.get(index).isString()
					.stringValue()
					+ getUnixTimeStamp());
			node.setAttribute("DisplayName", jerarquia.get(index).isString()
					.stringValue()
					+ "-" + getUnixTimeStamp());
			node.setAttribute("Padre", parent.getAttribute("DisplayRef"));

			node.setAttribute("DimName", jerarquia.get(index).isString()
					.stringValue());
			node.setAttribute("DimRef", parent.getAttribute("DimRef"));

			node.setAttribute("tipoModulo", parent.getAttribute("tipoModulo"));
			node.setAttribute("Modelo", parent.getAttribute("Modelo"));
			node.setAttribute("IDPeriodo", parent.getAttribute("IDPeriodo"));
			node.setAttribute("IDEscenario", parent.getAttribute("IDEscenario"));
			node.setAttribute("DriverName", "example");
			node.setAttribute("Costo", "");
			node.setAttribute("DQF", "0");

			String dimToInsert = jerarquia.get(index).isString().stringValue();

			// Si estamos en el ultimo nivel, el siguiente es nivel 1 de
			// siguiente dimension
			if (!parent.getAttribute("DimName").equals(dimToInsert)) {

				node.setAttribute("Nivel", String.valueOf(1));
				node.setAttribute("Padre_ID", "");

			} else {

				newLevel = Integer.valueOf(parent.getAttribute("Nivel")) + 1;
				node.setAttribute("Nivel", String.valueOf(newLevel));
				node.setAttribute("Padre_ID", parent.getAttribute("ID"));
			}

			multiDimView.getDataSource().addData(node, new DSCallback() {
				@Override
				public void execute(DSResponse response, Object rawData,
						DSRequest request) {

					if (response.getStatus() >= 0 && response.getData() != null
							&& response.getData().length > 0) {

						multiDimView.refreshData();
					}
				}
			});

			nodePosition++;
		}
	}

	private int getUnixTimeStamp() {

		Date date = new Date();
		int iTimeStamp = (int) (date.getTime() * .001);
		return iTimeStamp;
	}

}
