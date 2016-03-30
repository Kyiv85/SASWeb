package gwt.adminsas.client;

import gwt.adminsas.client.MainView.Dimensiones;
import gwt.adminsas.client.data.MultiDimensionDS;

import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONString;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.ResultSet;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.FetchDataEvent;
import com.smartgwt.client.widgets.events.FetchDataHandler;
import com.smartgwt.client.widgets.events.KeyPressEvent;
import com.smartgwt.client.widgets.events.KeyPressHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.grid.events.EditorExitEvent;
import com.smartgwt.client.widgets.grid.events.EditorExitHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.DataArrivedEvent;
import com.smartgwt.client.widgets.tree.events.DataArrivedHandler;
import com.smartgwt.client.widgets.tree.events.FolderClosedEvent;
import com.smartgwt.client.widgets.tree.events.FolderClosedHandler;
import com.smartgwt.client.widgets.tree.events.FolderDropEvent;
import com.smartgwt.client.widgets.tree.events.FolderDropHandler;
import com.smartgwt.client.widgets.tree.events.FolderOpenedEvent;
import com.smartgwt.client.widgets.tree.events.FolderOpenedHandler;

/**
 * Vista MultiDimensión de la aplicación SASWebAdmin
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class MultiDimensionGrid extends TreeGrid {

	private MultiDimensionDS multiDimDS;
	
	private AsignacionesView asigView;
	private MainToolbar mainToolbar;
	private DataFilterToolBar dataToolbar;
	
	private String modeloActual = null;
	private String tipoActual = "RESOURCE";
	String periodoActual = null;

	private String dimDestino = "default";

	private boolean newModel = false;

	String openStateTree = "";
	int iconIndex = 0;

	HashMap<String, String> icons = new HashMap<String, String>();

	private String[] dims = new String[] { "DIM1", "DIM2", "DIM3", "DIM4", "DIM5",
			"DIM6", "DIM7" };

	protected MultiDimensionGrid(AsignacionesView lig, DataFilterToolBar dtb,
			MainToolbar mtb) {

		multiDimDS = new MultiDimensionDS("MultiDimensionDS");
		
		// hack para dimensiones
		multiDimDS.setTree(this); 
		
		asigView = lig;
		dataToolbar = dtb;
		mainToolbar = mtb;

		// Configuración basica de la vista MultiDimensión
		setWidth100();

		setDataSource(multiDimDS);
		setCanEdit(true);
		setSelectionType(SelectionStyle.SINGLE);

		setLoadingDataMessage("Cargando información del Servidor");
		setAutoFetchData(false);

		setShowEmptyMessage(true);
		setEmptyMessage("No hay elementos que mostrar");

		setCanAcceptDroppedRecords(true);
		setShowCustomIconOpen(true);
		setShowCustomIconDrop(true);
		
		setEditPendingCSSText("color:red;border:1px solid red;");
		
		TreeGridField nameField = new TreeGridField("DisplayName");
		nameField.setWidth(450);

		TreeGridField displayRefField = new TreeGridField("DisplayRef");
		displayRefField.setHidden(false);
		displayRefField.setCanEdit(false);
		displayRefField.setWidth(175);

		TreeGridField padreField = new TreeGridField("Padre");
		padreField.setCanEdit(false);
		padreField.setHidden(true);

		TreeGridField dimNameField = new TreeGridField("DimName");
		dimNameField.setCanEdit(false);
		dimNameField.setHidden(true);

		TreeGridField dimRefField = new TreeGridField("DimRef");
		dimRefField.setCanEdit(false);
		dimRefField.setHidden(true);

		TreeGridField nivelField = new TreeGridField("Nivel");
		nivelField.setCanEdit(false);
		nivelField.setWidth(50);

		TreeGridField modeloField = new TreeGridField("Modelo");
		modeloField.setCanEdit(false);
		modeloField.setHidden(true);
		// quitar

		TreeGridField tipoField = new TreeGridField("tipoModulo");
		tipoField.setCanEdit(false);
		tipoField.setHidden(true);
		// quitar 

		TreeGridField idPeriodo = new TreeGridField("IDPeriodo");
		idPeriodo.setCanEdit(false);
		idPeriodo.setHidden(true);
		//quitar
		
		TreeGridField idEscenario = new TreeGridField("IDEscenario");
		idEscenario.setCanEdit(false);
		idEscenario.setHidden(true);
		// quitar

		TreeGridField ID = new TreeGridField("ID");
		ID.setCanEdit(true);
		ID.addEditorExitHandler(new EditorExitHandler() {
			
			@Override
			public void onEditorExit(EditorExitEvent event) {
			
				String newValue = (String)event.getNewValue();
		
				// Si contiene un dash, es vacia o contiene espacios
				if( newValue != null && (newValue.indexOf("-") >= 0 || newValue.equals("") || newValue.indexOf(" ") > 0) ){
	
					SC.warn("El ID del elemento no puede contener '-', espacios o ser vacío");
					event.cancel();
				}
		}});

		
		// Edicion de DQF en vista de asignaciones
		asigView.getDqfField().addEditorExitHandler(new EditorExitHandler() {
			@Override
			public void onEditorExit(EditorExitEvent event) {

				Map<String, Object> map = new HashMap<String, Object>();
				
				TreeNode origenRecord = (TreeNode) getSelectedRecord();
				ListGridRecord destinoRecord = asigView.getSelectedRecord();

				map.put("periodo", periodoActual);
				map.put("modelo", origenRecord.getAttribute("Modelo"));

				map.put("modulofuente", origenRecord.getAttribute("tipoModulo"));
				map.put("dimensionfuente", origenRecord.getAttribute("DisplayRef"));

				map.put("modulodestino",
						destinoRecord.getAttribute("tipoModulo"));
				map.put("dimensiondestino",
						destinoRecord.getAttribute("referencia"));

				map.put("conductor", destinoRecord.getAttribute("conductor"));

				asigView.getDataSource().setDefaultParams(map);
			}
		});
		

		asigView.addEditCompleteHandler(new EditCompleteHandler() {
			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				
				Map<String, Object> map = new HashMap<String, Object>();
				asigView.getDataSource().setDefaultParams(map);

				TreeNode node = (TreeNode) getSelectedRecord();
				
				asigView.invalidateCache();
				
				asigView.refreshData(node.getAttribute("DisplayRef"),
						node.getAttribute("Modelo"));
			}
		});
		
		TreeGridField padreID = new TreeGridField("Padre_ID");
		padreID.setCanEdit(false);
		padreID.setHidden(true);

		TreeGridField editNow = new TreeGridField("editNow");
		editNow.setCanEdit(false);
		editNow.setHidden(true);
		// quitar
		
		// Al terminar de editar, refrescar arbol
		addEditCompleteHandler(new EditCompleteHandler() {
			public void onEditComplete(EditCompleteEvent event) {

				openStateTree = getOpenState();

				JSONString st = (JSONString) multiDimDS.getJerarquia().get(0);
				dimDestino = st.stringValue();

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("dimActual", dimDestino);
				getDataSource().setDefaultParams(map);

				invalidateCache();
				refreshData();
			}
		});
		
		// Salvamos el estado de el arbol
		addFolderOpenedHandler(new FolderOpenedHandler() {
			public void onFolderOpened(FolderOpenedEvent event) {
				openStateTree = getOpenState();

				Log.info("folder opened:"
						+ event.getNode().getAttribute("DimName"));
				dimDestino = event.getNode().getAttribute("DimName");

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("dimActual", dimDestino);
				getDataSource().setDefaultParams(map);
		
			}
		});
		
		addFolderClosedHandler(new FolderClosedHandler() {
			public void onFolderClosed(FolderClosedEvent event) {
				openStateTree = getOpenState();
			}
		});

		// En caso de drop
		addFolderDropHandler(new FolderDropHandler() {
			@Override
			public void onFolderDrop(FolderDropEvent event) {

				openStateTree = getOpenState();
				
				Log.info("folder dropped:"
						+ event.getFolder().getAttribute("DimName"));

				dimDestino = event.getFolder().getAttribute("DimName");
				String dimNodoActual = event.getNodes()[0]
						.getAttribute("DimName");

				// Comportamiento valido
				if (dimDestino.equalsIgnoreCase(dimNodoActual)) {

					Map<String, Object> map = new HashMap<String, Object>();
					map.put("IDDropped", event.getFolder().getAttribute("ID"));
					map.put("dimActual", dimDestino);

					getDataSource().setDefaultParams(map);

					Log.info("dim actual = dimNodo");

					invalidateCache();
					refreshData();

				} else {
					SC.warn("El elemento no puede moverse fuera de su propia dimensión");
					event.cancel();
				}

			}
		});

		// Borrar con Delete key
		addKeyPressHandler(new KeyPressHandler() {

			public void onKeyPress(KeyPressEvent event) {
				String pressed = EventHandler.getKey();

				if (pressed.equalsIgnoreCase("Delete")) {

					final TreeNode node = (TreeNode) getSelectedRecord();
					SC.confirm(
							"Está seguro que desea borrar "
									+ node.getAttribute("DisplayName") + "?",
							new BooleanCallback() {
								public void execute(Boolean value) {
									if (value != null && value) {
										removeSelectedData();
										refreshData();
									}
								}
							});
				}
			}
		});

		// carga de datos al arbol multidimensión
		addDataArrivedHandler(new DataArrivedHandler() {
			public void onDataArrived(DataArrivedEvent event) {

				int editRow = 0;
				boolean editable = false;
				// se obtiene el arbol
				Tree tree = getData();
				
				// Trae el parent que inicia la busqueda
				TreeNode parentNode = event.getParentNode();

				// Todos los nodos
				TreeNode[] nodes = tree.getAllNodes(parentNode);
				
				// Otro color para los hijos
				for (TreeNode s : nodes) {

					setIconAndLevel(s);
					
					if (s.getAttribute("editNow") != null) {
						editRow = getRecordIndex(s);
						editable = true;
						s.setAttribute("editNow", (Object) null);
					}
				}

				// si es un fetch de data de nuevo modelo
				if (newModel)
					newModel = false;

				setOpenState(openStateTree);
				
				if(!tree.isOpen(parentNode))
					tree.openFolder(parentNode);

				if (editable) {
					boolean editing = startEditing(editRow, 0, false);
					Log.info("startEditing:" + editing);
				}

			}
		});
		
		// Se pobla vista de Asignaciones dependiendo del record seleccionado
		// se habilita inserción de nodos, contextualmente
		addRecordClickHandler(new RecordClickHandler() {

			@Override
			public void onRecordClick(RecordClickEvent event) {

				// Obtener el nodo seleccionado
				TreeNode node = (TreeNode) getSelectedRecord();

				// Todos los botones deshabilitados por defecto
				mainToolbar.dim1Button.disable();
				mainToolbar.dim2Button.disable();
				mainToolbar.dim3Button.disable();
				mainToolbar.dim4Button.disable();
				mainToolbar.dim5Button.disable();
				mainToolbar.dim6Button.disable();

				MultiDimensionDS ds = (MultiDimensionDS) getDataSource();
				JSONArray jerarquia = ds.getJerarquia();

				// Se habilitan los botones de agregar nodos
				// dependiendo de donde estemos parados
				switch (Dimensiones.valueOf(node.getAttribute("Dim"))) {
				case DIM1:

					mainToolbar.dim1Button.enable();

					// Si existe una siguiente dimension a la actual
					if (jerarquia.get(1) != null)
						mainToolbar.dim2Button.enable();

					break;
				case DIM2:

					mainToolbar.dim2Button.enable();

					// Si existe una siguiente dimension a la actual
					if (jerarquia.get(2) != null)
						mainToolbar.dim3Button.enable();

					break;
				case DIM3:

					mainToolbar.dim3Button.enable();

					// Si existe una siguiente dimension a la actual
					if (jerarquia.get(3) != null)
						mainToolbar.dim4Button.enable();

					break;
				case DIM4:

					mainToolbar.dim4Button.enable();

					// Si existe una siguiente dimension a la actual
					if (jerarquia.get(4) != null)
						mainToolbar.dim5Button.enable();

					break;
				case DIM5:

					mainToolbar.dim5Button.enable();

					// Si existe una siguiente dimension a la actual
					if (jerarquia.get(5) != null)
						mainToolbar.dim6Button.enable();

					break;
				case DIM6:
					mainToolbar.dim6Button.enable();
					break;
				}
				// Si es un leaf
				if (node.getAttribute("isLeaf") != null
						&& mainToolbar.voyPara.isSelected()) {

					Map<String, Object> map = new HashMap<String, Object>();
					asigView.getDataSource().setDefaultParams(map);

					asigView.invalidateCache();
					asigView.refreshData(node.getAttribute("DisplayRef"),
							node.getAttribute("Modelo"));
					dataToolbar.asignacion.setDisabled(false);
					dataToolbar.eliminarAsignacion.setDisabled(false);

				} else {

					if (mainToolbar.voyPara.isSelected()) {
						dataToolbar.asignacion.setDisabled(true);
						dataToolbar.eliminarAsignacion.setDisabled(true);
					}
					
					asigView.setData(new ListGridRecord[] {});
				}
			}
		});
		setFields(nameField, displayRefField, ID, dimNameField,
				dimRefField, padreField, padreID,
				nivelField, modeloField, tipoField, idPeriodo,
				idEscenario, editNow);
		draw();
	}

	protected void refreshData() {
		newModel = true;

		Log.info("RefreshData()");

		Criteria crit = new Criteria("modelo", modeloActual);
		crit.addCriteria("periodoScenario", periodoActual);
		crit.addCriteria("tipo", tipoActual);
		
		fetchData(crit);
	}

	/**
	 * Llamada para expansión de arbol
	 */
	protected void refreshData(String modelo, String periodoScenario, String tipo) {

		tipoActual = tipo;
		modeloActual = modelo;
		periodoActual = periodoScenario;
		newModel = true;

		Log.info("RefreshData(modelo, periodoscenario and stuff)");

		Criteria crit = new Criteria("modelo", modelo);
		crit.addCriteria("periodoScenario", periodoScenario);
		crit.addCriteria("tipo", tipo);
		
		fetchData(crit);
	}
	
	public void setIconAndLevel(TreeNode s) {

		String dimension = s.getAttribute("DimName");

		// si la dimension existe en el hashmap
		if (icons.containsKey(dimension)) {

			String curIcon = icons.get(dimension);

			s.setAttribute("icon",
					"/SASWeb/app/img/botones/"
							+ dims[Integer.parseInt(curIcon)] + ".png");
			s.setAttribute("Dim", dims[Integer.parseInt(curIcon)]);

		} else {

			icons.put(dimension, String.valueOf(iconIndex));
			String curIcon = icons.get(dimension);

			s.setAttribute("icon",
					"/SASWeb/app/img/botones/"
							+ dims[Integer.parseInt(curIcon)] + ".png");
			s.setAttribute("Dim", dims[Integer.parseInt(curIcon)]);
			iconIndex++;
		}

		// Para las hamburguesas
		if (s.getAttribute("isLeaf") != null
				&& s.getAttribute("UltDimension").equals(
						s.getAttribute("DimName"))) {

			s.setCanExpand(false);

			if (s.getAttribute("tipoModulo").equalsIgnoreCase("RESOURCE")) {

				s.setAttribute("icon", "/SASWeb/app/img/botones/recurso.png");
			} else if (s.getAttribute("tipoModulo").equalsIgnoreCase(
					"COSTOBJECT")) {

				s.setAttribute("icon",
						"/SASWeb/app/img/botones/objetoCosto.png");
			} else {
				s.setAttribute("icon", "/SASWeb/app/img/botones/actividad.png");
			}
		}
	}

	public void clearAllGrids(){
		
		Tree tree = getTree();
		tree.removeList(tree.getAllNodes());

		asigView.setData(new ListGridRecord[] {});
	}
	
	public Criteria getInitialCriteria() {
		
		Criteria crit = new Criteria("modelo", modeloActual);
		crit.addCriteria("periodoScenario", periodoActual);
		crit.addCriteria("tipo", tipoActual);
		return crit;
	}

	public String getOpenStateTree() {
		return openStateTree;
	}

	public void setOpenStateTree(String openStateTree) {
		this.openStateTree = openStateTree;
	}
	
	public void setCurrentModel(String modelo) {
		modeloActual = modelo;
	}

	public String getCurrentModel() {
		return modeloActual;
	}
	
}
