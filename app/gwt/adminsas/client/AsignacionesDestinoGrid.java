package gwt.adminsas.client;

import gwt.adminsas.client.data.AsignacionesDestinoDS;

import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.DataArrivedEvent;
import com.smartgwt.client.widgets.tree.events.DataArrivedHandler;
import com.smartgwt.client.widgets.tree.events.FolderOpenedEvent;
import com.smartgwt.client.widgets.tree.events.FolderOpenedHandler;

public class AsignacionesDestinoGrid extends TreeGrid {

	private AsignacionesDestinoDS destinoDS;

	private String dimDestino = "default";

	HashMap<String, String> icons = new HashMap<String, String>();
	int iconIndex = 0;

	private String[] dims = new String[] { "DIM1", "DIM2", "DIM3", "DIM4",
			"DIM5", "DIM6", "DIM7" };

	public AsignacionesDestinoGrid() {

		destinoDS = AsignacionesDestinoDS.getInstance();
		setDataSource(destinoDS);

		setWidth100();
		setHeight(200);

		TreeGridField field1 = new TreeGridField("DisplayName", "Nombre");
		field1.setCanSort(false);

		TreeGridField field2 = new TreeGridField("ID", "Referencia");
		field2.setCanSort(false);

		setFields(field1, field2);

		// Salvamos el estado de el arbol
		addFolderOpenedHandler(new FolderOpenedHandler() {
			public void onFolderOpened(FolderOpenedEvent event) {

				Log.info("folder opened asignaciones:"
						+ event.getNode().getAttribute("DimName"));
				dimDestino = event.getNode().getAttribute("DimName");

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("dimActual", dimDestino);
				getDataSource().setDefaultParams(map);

			}
		});

		// Carga de datos al arbol multidimensión
		addDataArrivedHandler(new DataArrivedHandler() {
			public void onDataArrived(DataArrivedEvent event) {

				// se obtiene el arbol
				Tree tree = getData();

				// Trae el no parent que inicia la busqueda
				TreeNode node = event.getParentNode();

				// Todos los nodos
				TreeNode[] nodes = tree.getAllNodes(node);

				// Otro color para los hijos
				for (TreeNode s : nodes) {

					String dimension = s.getAttribute("DimName");

					// si la dimension existe en el hashmap
					if (icons.containsKey(dimension)) {

						String curIcon = icons.get(dimension);

						s.setAttribute("icon", "/SASWeb/app/img/botones/"
								+ dims[Integer.parseInt(curIcon)] + ".png");
						s.setAttribute("Dim", dims[Integer.parseInt(curIcon)]);

					} else {

						icons.put(dimension, String.valueOf(iconIndex));
						String curIcon = icons.get(dimension);

						s.setAttribute("icon", "/SASWeb/app/img/botones/"
								+ dims[Integer.parseInt(curIcon)] + ".png");
						s.setAttribute("Dim", dims[Integer.parseInt(curIcon)]);
						iconIndex++;
					}

					// Para las hamburguesas
					if (s.getAttribute("isLeaf") != null
							&& s.getAttribute("UltDimension").equals(
									s.getAttribute("DimName"))) {

						s.setCanExpand(false);

						if (s.getAttribute("tipoModulo").equalsIgnoreCase(
								"RESOURCE")) {

							s.setAttribute("icon",
									"/SASWeb/app/img/botones/recurso.png");
						} else if (s.getAttribute("tipoModulo")
								.equalsIgnoreCase("COSTOBJECT")) {

							s.setAttribute("icon",
									"/SASWeb/app/img/botones/objetoCosto.png");
						} else {
							s.setAttribute("icon",
									"/SASWeb/app/img/botones/actividad.png");
						}
					}

				}
			}
		});
	}

	protected void refreshData(String modelo, String periodoScenario,
			String tipo) {

		Criteria crit = new Criteria("modelo", modelo);
		crit.addCriteria("periodoScenario", periodoScenario);
		crit.addCriteria("tipo", tipo);

		fetchData(crit);
	}

}
