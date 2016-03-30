package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jobs.Jobs;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import com.google.gson.Gson;

import db.Driver;

/**
 * Controlador de Administrador
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
@With(Secure.class)
public class Administrador extends Controller {

	static String currentEditable = "";
	final static int DIMMEMBER_REFS = 12;

	public static final String DIMENSIONRELACION_JOB_CS = "transformaciones\\RH_A_TMP\\TMP_A_TMP_MIEMBRO_DIMENSION_TEMPORAL\\CS.kjb";

	public static final String DIMENSIONRELACION_JOB_TI = "transformaciones\\RH_A_TMP\\TMP_A_TMP_MIEMBRO_DIMENSION_TEMPORAL\\TI.kjb";

	public static final String DIMENSIONRELACION_JOB_CORP = "transformaciones\\RH_A_TMP\\TMP_A_TMP_MIEMBRO_DIMENSION_TEMPORAL\\CORP.kjb";

	public static final String DIMENSIONRELACION_JOB_RED = "transformaciones\\RH_A_TMP\\TMP_A_TMP_MIEMBRO_DIMENSION_TEMPORAL\\RED.kjb";

	public static final String DIMENSIONRELACION_JOB_AP = "transformaciones\\RH_A_TMP\\TMP_A_TMP_MIEMBRO_DIMENSION_TEMPORAL\\AP.kjb";

	public static final String DIMENSIONRELACION_JOB_COM = "transformaciones\\RH_A_TMP\\TMP_A_TMP_MIEMBRO_DIMENSION_TEMPORAL\\COM.kjb";

	/**
	 * Proveedor de datos para la vista MultiDimension
	 */
	public static void getMultiDimensionData() {

		int dimActualPadre = 0;

		String periodo = null;
		String scenario = null;

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> response = new HashMap<String, Object>();

		Driver driver = new Driver();

		String padre = (params.get("Padre") != null ? params.get("Padre") : "");
		String padreDim = (params.get("dimActual") != null ? params
				.get("dimActual") : "");
		String tipo = (params.get("tipo") != null ? params.get("tipo")
				: "RESOURCE");
		String modelo = (params.get("modelo") != null ? params.get("modelo")
				: "tmp_cs");

		// Separamos la data en periodo y escenario
		if (params.get("periodoScenario") != null) {
			String[] temp = params.get("periodoScenario").split("/");
			periodo = temp[0].trim();
			scenario = temp[1].trim();
		}

		// Orden o jerarquia de dimensiones
		ArrayList<HashMap<String, String>> jerarquiaDim = getJerarquiaDimensiones(
				modelo, tipo, periodo, scenario);

		// Obtenemos la maxima dimension
		HashMap<String, String> maxDimension = getMaxDimension(modelo, tipo);

		// Obtenemos la dimension actual del nodo Padre (el nodo actual)
		HashMap<String, String> padreCurrentDim = getNodeCurrentDimension(
				modelo, padreDim, tipo);

		String padreID = padre;
		if (padreCurrentDim != null && !padre.equals("")) {
			dimActualPadre = Integer.parseInt(padreCurrentDim.get("indexDim"));
			String[] padreSlideIDs = padre.split("-");
			padreID = padreSlideIDs[dimActualPadre - 1];
		}

		// Obtenemos data para el arbol multidimensión
		ArrayList<HashMap<String, String>> result = driver
				.getMultiDimensionViewData(modelo, padreID, periodo, scenario,
						tipo, padreDim);

		Iterator<HashMap<String, String>> multiDimIt = result.iterator();
		while (multiDimIt.hasNext()) {

			HashMap<String, String> node = multiDimIt.next();

			// Identificar Maximo nivel por cada Dimension
			int maxNivelDim = driver.getMaxNivelDimension(modelo,
					(String) node.get("DimName"));

			HashMap<String, String> currentDim = getCurrentDimension(modelo,
					node.get("DimName"), node.get("tipoModulo"));

			node.put("UltNivel", String.valueOf(maxNivelDim));
			node.put("UltDimension", maxDimension.get("_ID"));

			// Se construye el id intersección para la unicidad de los elementos
			// en el arbol
			String interID = parseIntersectionID(node.get("DisplayRef"),
					jerarquiaDim.size(),
					Integer.parseInt(currentDim.get("indexDim")), padre,
					dimActualPadre);
			node.put("DisplayRef", interID);
			node.put("Padre", padre);

			if (currentEditable.equals(node.get("ID"))) {
				node.put("editNow", "true");
				currentEditable = "";

			}
			// Identificar las hojas
			if (isLeaf(modelo, node.get("ID")))
				node.put("isLeaf", "true");
		}

		map.put("startRows", 0);
		map.put("endRow", 0);

		// De haber o no haber resultados
		if (result != null) {
			map.put("status", 0);
			map.put("totalRows", result.size());
		}

		// Dimensiones y Jerarquia respectiva (Jerarquia_Dimension)
		ArrayList<String> jer = new ArrayList<String>();

		for (Iterator iterator = jerarquiaDim.iterator(); iterator.hasNext();) {
			HashMap<String, String> hashMap = (HashMap<String, String>) iterator
					.next();
			jer.add(hashMap.get("_ID"));
		}

		map.put("jerarquia", jer);
		map.put("data", result);
		response.put("response", map);

		renderJSON(response);
	}

	/**
	 * Operaciones CRUD sobre la vista MultiDimension
	 */
	public static void manageMultiDimensionRequests() {

		String inserts = "", childrenSQL = "", childrenRelacionSQL = "", updateMDSQL = "", updateMDRelacionSQL = "", modelo = "", modelName = "";

		String periodo = "";
		String escenario = "";
		String tipo = "";

		boolean success = false;

		Driver driver = new Driver();
		String operation = params.get("_operationType");

		Gson gson = new Gson();

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> response = new HashMap<String, Object>();

		// UPDATE
		if (operation.equals("update")) {

			String dimName = params.get("DimName");

			if (dimName != null) {

				/*
				 * Modificar Data / Reparenting
				 */
				Logger.info("Update Action/Reparenting via POST");

				modelo = params.get("Modelo");
				String padre = params.get("IDDropped");
				String padreID = params.get("Padre");
				periodo = params.get("IDPeriodo");
				escenario = params.get("IDEscenario");
				tipo = params.get("tipoModulo");
				String idNode = params.get("ID");

				modelo = driver.getModelNameAsDB(modelo);

				// Nodo al que fue movido el nodo original
				HashMap<String, String> parent = driver
						.getAdminViewRowTemporal(modelo, padre, periodo,
								escenario, tipo);

				// displayRefParent es el nuevo Padre
				String displayRefParent = parent.get("ID");

				// Si las dimensiones del nodo a reparentar y el padre son
				// iguales, se puede mover
				if (dimName.equals(parent.get("DimName"))) {

					// Modificar Padre_ID
					updateMDSQL = "UPDATE " + modelo
							+ ".dbo.miembroDimension SET Padre_ID = '"
							+ displayRefParent + "' " + "where ID  = '"
							+ idNode + "' ;";

					Logger.info(updateMDSQL);
					success = driver._transactionWithoutResult(updateMDSQL);

					// Obtener registro modificado para construir response
					HashMap<String, String> result = driver
							.getMultiDimensionViewNode(modelo, idNode, periodo,
									escenario, tipo);

					// Si hay resultados o no devuelve filas
					if (success) {

						runRelacionDimensionJob(modelo);

						HashMap<String, String> currentDim = getCurrentDimension(
								modelo, dimName, tipo);

						// Obtenemos la dimension actual del nodo Padre
						HashMap<String, String> padreCurrentDim = getNodeCurrentDimension(
								modelo, dimName, tipo);
						int dimActualPadre = Integer.parseInt(padreCurrentDim
								.get("indexDim"));

						String interID = parseIntersectionID(
								result.get("ID"),
								getJerarquiaDimensiones(modelo, tipo, periodo,
										escenario).size(),
								Integer.parseInt(currentDim.get("indexDim")),
								padre, dimActualPadre);

						// ID y Padre. Concatenado
						result.put("DisplayRef", interID);
						result.put("Padre", padreID);

						map.put("data", result);
						map.put("status", 0);
						map.put("totalRows", result.size());

					} else { // Si hay un error
						map.put("status", -1);
						map.put("totalRows", 1);
					}

					map.put("startRows", 0);
					map.put("endRow", 0);
					response.put("response", map);

					renderJSON(response);

				} else {
					// Dimensiones distintas, reparenting ilegal

					map.put("status", -1);
					map.put("data",
							"El elemento no puede moverse fuera de su misma dimensión");
					response.put("response", map);
					renderJSON(response);
				}

			} else {

				/*
				 * Modificar Data /Normal
				 */
				Logger.info("Update Action via POST");

				// Obtenemos valores antes de la edición
				TreeNode decoded = gson.fromJson(params.get("_oldValues"),
						TreeNode.class);

				String idRef = params.get("ID");
				String displayName = params.get("DisplayName");

				// Valores base para el update
				String oldID = decoded.ID;

				periodo = decoded.IDPeriodo;
				escenario = decoded.IDEscenario;
				tipo = decoded.tipoModulo;

				modelName = decoded.Modelo;
				// Dimension actual, basado en _oldValues
				dimName = decoded.DimName;
				modelo = driver.getModelNameAsDB(modelName);

				// Caso especial: Si el record actual es CR y modificamos idRef
				if (idRef != null && dimName.equalsIgnoreCase("CR")) {

					// Encontrar duplicados en CR
					String uniqueDimensionCRSQL = "select _NombreModelo as NombreModelo from tmp_cs.dbo.miembrodimension "+
							"where ID= '" + idRef + "' " +
							"AND _IDDimension = 'CR' "+ 
							"union "+
							"select _NombreModelo as NombreModelo from tmp_ti.dbo.miembrodimension "+
							"where ID= '"+ idRef+ "'" +
						    " AND _IDDimension = 'CR' "+
							"union "+
							"select _NombreModelo as NombreModelo from tmp_red.dbo.miembrodimension " +
							"where ID= '"+ idRef+ "'" +
							"AND _IDDimension = 'CR' "+
							"union "+
							"select _NombreModelo as NombreModelo from tmp_com.dbo.miembrodimension "+
							"where ID= '" + idRef + "'" + 
							"AND _IDDimension = 'CR' "+
							"union "+
							"select _NombreModelo as NombreModelo from tmp_ap.dbo.miembrodimension " +
							"where ID= '" + idRef + "'"  +
						    "AND _IDDimension = 'CR' "+
							"union "+
							"select _NombreModelo as NombreModelo from tmp_corp.dbo.miembrodimension "+
							"where ID= '"+ idRef + "' "+ 
							"AND _IDDimension = 'CR' ";

					HashMap<String, String> duplicatedDims = driver
							._queryWithResult(uniqueDimensionCRSQL);

					if (duplicatedDims != null) {

						map.put("status", -1);
						map.put("data",
								"El Registro " + idRef
										+ " ya existe en el modelo "
										+ duplicatedDims.get("NombreModelo")
										+ ". Favor elegir otro ID");
						map.put("startRows", 0);
						map.put("endRow", 0);
						map.put("totalRows", 1);
						response.put("response", map);
						renderJSON(response);
					}
				}

				// Construcción del query
				if (displayName != null && idRef != null)
					inserts = "ID = '" + idRef + "', Nombre = '" + displayName
							+ "'";
				else if (idRef != null)
					inserts += "ID = '" + idRef + "' ";
				else if (displayName != null)
					inserts += "Nombre = '" + displayName + "'";

				// Modificar nombre y/o DisplayRef
				updateMDSQL = "UPDATE " + modelo + ".dbo.miembroDimension SET "
						+ inserts + " " + "where ID  = '" + oldID + "' ;";

				updateMDRelacionSQL = "UPDATE " + modelo
						+ ".dbo.miembroDimensionrelaciondimension SET "
						+ inserts + " " + "where ID  = '" + oldID + "' ;";

				// Si se modifico el displayRef
				if (idRef != null) {
					// Modificar los hijos, apuntar a ID nuevo de padre
					childrenSQL = "UPDATE " + modelo
							+ ".dbo.miembroDimension SET Padre_ID = '" + idRef
							+ "' where Padre_ID  = '" + oldID + "' ;";

					childrenRelacionSQL = "UPDATE "
							+ modelo
							+ ".dbo.miembroDimensionrelaciondimension SET Padre_ID = '"
							+ idRef + "' where Padre_ID  = '" + oldID + "' ;";

				} else {
					idRef = oldID;
				}

				Logger.info(updateMDSQL);
				Logger.info(updateMDRelacionSQL);
				Logger.info(childrenSQL);
				Logger.info(childrenRelacionSQL);

				success = driver._transactionWithoutResult(updateMDSQL,
						childrenSQL, updateMDRelacionSQL, childrenRelacionSQL);
				// Si hay resultados o no devuelve filas
				if (success) {

					// Obtener registro modificado para construir response
					HashMap<String, String> result = driver
							.getMultiDimensionViewNode(modelo, idRef,
									(String) decoded.IDPeriodo,
									(String) decoded.IDEscenario,
									(String) decoded.tipoModulo);

					currentEditable = "";
					map.put("status", 0);
					map.put("totalRows", result.size());
					map.put("data", result);

				} else {
					// Si hay un error
					map.put("status", -1);
					map.put("data",
							"No pueden existir DisplayReference duplicados. Valores no fueron almacenados."
									+ "Favor corregir datos resaltados en azul.");

				}

				map.put("startRows", 0);
				map.put("endRow", 0);
				map.put("totalRows", 1);
				response.put("response", map);

				renderJSON(response);
			}

		} else if (operation.equals("add")) {

			/*
			 * Agregar Data
			 */

			Logger.info("Add Action via POST");

			boolean isETLSuccess = true;

			// Valores base para el insert
			String idRef = params.get("ID");

			String padre = params.get("Padre");
			String padreID = (params.get("Padre_ID") != null ? params
					.get("Padre_ID") : "");

			modelName = params.get("Modelo");

			periodo = params.get("IDPeriodo");
			escenario = params.get("IDEscenario");
			tipo = params.get("tipoModulo");

			String dimName = params.get("DimName");
			String displayName = params.get("DisplayName");

			dimName = params.get("DimName");
			modelo = driver.getModelNameAsDB(modelName);

			// Se inserta el nodo
			String sqlInsert = " INSERT into "
					+ modelo
					+ ".dbo.miembroDimension(_NombreModelo, _IDDimension, ID, Nombre, Padre_ID, "
					+ " _IDPeriodo, _IDEscenario ) " + " VALUES('"
					+ params.get("Modelo") + "', '" + dimName + "','" + idRef
					+ "', '" + displayName + "', '" + padreID + "', " + " '"
					+ periodo + "', '" + escenario + "');";

			Logger.info(sqlInsert);
			success = driver._transactionWithoutResult(sqlInsert);

			// Obtener registro modificado para construir response
			HashMap<String, String> result = new HashMap<String, String>();

			isETLSuccess = runRelacionDimensionJob(modelo);

			// Si hay resultados o no devuelve filas
			if (success && isETLSuccess) {

				result = driver.getMultiDimensionViewNode(modelo, idRef,
						periodo, escenario, tipo);
				HashMap<String, String> currentDim = getCurrentDimension(
						modelo, dimName, tipo);

				// Obtenemos la dimension actual del nodo Padre
				HashMap<String, String> padreCurrentDim = getNodeCurrentDimension(
						modelo, dimName, tipo);
				int dimActualPadre = Integer.parseInt(padreCurrentDim
						.get("indexDim"));

				// Construir DisplayRef, Padre (concatenados)
				String interID = parseIntersectionID(
						result.get("ID"),
						getJerarquiaDimensiones(modelo, tipo, periodo,
								escenario).size(),
						Integer.parseInt(currentDim.get("indexDim")), padre,
						dimActualPadre);

				// Identificar Maximo nivel por cada Dimension
				result.put("UltNivel", String.valueOf(driver.getMaxNivelDimension(
						modelo, (String) result.get("DimName"))));

				// Obtenemos la maxima dimension
				HashMap<String, String> maxDimension = getMaxDimension(modelo,
						tipo);
				result.put("UltDimension", maxDimension.get("_ID"));

				// ID y Padre. Concatenado
				result.put("DisplayRef", interID);
				result.put("Padre", padre);

				// Identificar las hojas
				if (isLeaf(modelo, result.get("ID")))
					result.put("isLeaf", "true");

				// Guardamos el id del elemento a editar
				currentEditable = result.get("ID");

				map.put("status", 0);
				map.put("data", result);

			} else if (success && !isETLSuccess) { // Si hay un error

				map.put("status", -1);
				map.put("data",
						"Ocurrió un error al actualizar las relaciones entre dimensiones");

			} else {
				map.put("status", -1);
				map.put("data",
						"Ya existe un miembrodimension con esa referencia");

			}

			map.put("startRows", 0);
			map.put("endRow", 0);
			map.put("totalRows", 1);

			response.put("response", map);
			renderJSON(response);
		}
		if (operation.equals("remove")) {

			/*
			 * Remover Data
			 */

			Logger.info("Remove via POST");

			// Valores base para el remove
			String idRef = params.get("ID");
			modelName = params.get("Modelo");
			periodo = params.get("IDPeriodo");
			escenario = params.get("IDEscenario");
			tipo = params.get("tipoModulo");

			modelo = driver.getModelNameAsDB(modelName);

			// Se borra el nodo
			String sqlDelete = "DELETE FROM " + modelo
					+ ".dbo.miembroDimension " + "where ID  = '" + idRef
					+ "' ;";

			Logger.info(sqlDelete);
			success = driver._transactionWithoutResult(sqlDelete);

			map.put("data", "");

			if (success) {
				deleteChildren(modelo, modelName, periodo, escenario, idRef);
				map.put("status", 0);

				runRelacionDimensionJob(modelo);

			} else { // Si hay un error
				map.put("status", -1);
				map.put("data",
						"Hubo un error critico al borrar los registros, Por favor revisar la bitacora"
								+ "para mayor información");
			}

			map.put("startRows", 0);
			map.put("endRow", 0);
			map.put("totalRows", 1);
			response.put("response", map);

			renderJSON(response);
		}
	}

	static String parseIntersectionID(String displayref, int dimSize,
			int dimActual, String padreInterseccion, int padreDimActual) {

		StringBuffer buf = new StringBuffer();

		// Nodo sin padre
		if (padreInterseccion.equals("")) {

			for (int i = 0; i < dimSize; i++) {

				if (i == dimActual - 1)
					buf.append(displayref);
				else
					buf.append("ALL");

				if (i != dimSize - 1)
					buf.append("-");
			}

		} else { // Nodo con padre

			int j = 0;

			String[] intersectionParts = padreInterseccion.split("-");

			if (padreDimActual != 0) {
				// si va el Id del padre
				for (j = 0; j < dimActual - 1; j++) {
					buf.append(intersectionParts[j]);
					buf.append("-");
				}

				for (int i = j; i < dimSize; i++) {

					// Si estamos en la casilla donde va el ID
					if (i == dimActual - 1)
						buf.append(displayref);
					else
						// Si no va nada alli
						buf.append("ALL");

					if (i != dimSize - 1)
						buf.append("-");
				}
			}
		}
		return buf.toString();
	}

	static boolean deleteChildren(String bd, String modelo, String periodo,
			String scenario, String ID) {

		// Jerarquia de las dimensiones
		String childrenSQL = "select ID FROM " + bd + ".dbo.miembroDimension"
				+ " where 	_NombreModelo = '" + modelo + "'"
				+ " and _IDPeriodo = '" + periodo + "' "
				+ "	and _IDEScenario = '" + scenario + "'"
				+ "	and Padre_ID = '" + ID + "'";

		Driver driver = new Driver();
		Logger.info(childrenSQL);

		// Dimensiones y Jerarquia respectiva (Jerarquia_Dimension)
		ArrayList<HashMap<String, String>> result = driver
				._queryWithManyResults(childrenSQL);

		if (result != null) {

			for (Iterator iterator = result.iterator(); iterator.hasNext();) {
				HashMap<String, String> hashMap = (HashMap<String, String>) iterator
						.next();
				deleteChildren(bd, modelo, periodo, scenario, hashMap.get("ID"));

				String deleteChildSQL = "DELETE FROM " + bd
						+ ".dbo.miembroDimension" + " where 	_NombreModelo = '"
						+ modelo + "'" + " and _IDPeriodo = '" + periodo + "' "
						+ "	and _IDEScenario = '" + scenario + "'"
						+ "	and ID = '" + hashMap.get("ID") + "'";

				Logger.info(deleteChildSQL);
				driver._transactionWithoutResult(deleteChildSQL);
			}
		}
		return true;
	}

	/*
	 * Devuelve la Jerarquia de las dimensiones para determinado modelo
	 */
	static ArrayList<HashMap<String, String>> getJerarquiaDimensiones(
			String modelo, String tipo, String periodo, String scenario) {

		Driver driver = new Driver();

		String jerarquiaSQL = "select _ID, Jerarquia_Dimension from " + modelo
				+ ".dbo.ordenDimension" + " where 	_TipoModuloABC = '" + tipo
				+ "'" + " and _IDPeriodo = '" + periodo + "' "
				+ "	and _IDEScenario = '" + scenario + "'"
				+ " order by Jerarquia_Dimension";

		ArrayList<HashMap<String, String>> result = driver
				._queryWithManyResults(jerarquiaSQL);
		return result;
	}

	/**
	 * Proveedor de datos para la vista Asignaciones
	 */
	public static void getAsignacionesData() {

		Driver driver = new Driver();
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> response = new HashMap<String, Object>();

		String referencia = params.get("referencia");
		String modelo = params.get("modelo");
		
		String bd = driver.getModelNameAsDB(modelo);

		String sql = "SELECT DQF as dqf, DimensionDestino as referencia, _TipoModuloABCDestino as tipoModulo," +
				" _NombreConductor as conductor from "
				+ bd
				+ ".dbo.asignacionTemporal "
				+ "WHERE DimensionFuente = '"
				+ referencia + "'";
		
		Logger.info(sql);

		ArrayList<HashMap<String, String>> result = driver
				._queryWithManyResults(sql);
		Iterator<HashMap<String, String>> asigIt = result.iterator();

		while (asigIt.hasNext()) {

			HashMap<String, String> node = asigIt.next();

			String[] refSplit = node.get("referencia").split("-");

			String refNameSQL = "select Nombre from " + bd
					+ ".dbo.miembroDimension" + " where id = '"
					+ refSplit[refSplit.length - 1] + "'";

			Logger.info(refNameSQL);

			HashMap<String, String> maxNivelDim = driver
					._queryWithResult(refNameSQL);

			Logger.info(maxNivelDim.get("Nombre"));

			node.put("nombre", maxNivelDim.get("Nombre"));
		}

		map.put("startRows", 0);
		map.put("endRow", 0);
		map.put("data", result);
		response.put("response", map);

		renderJSON(response);
	}

	/**
	 * Recibe peticiones desde vista Asignaciones
	 */
	public static void manageAsignacionesRequests() {

		StringBuffer sql = new StringBuffer();
		boolean success = false;

		Driver driver = new Driver();
		String operation = params.get("_operationType");

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> response = new HashMap<String, Object>();

		Logger.info("manageAsignacionesRequests");

		// UPDATE
		if (operation.equals("add")) {

			String modelo = params.get("modelo");
			String periodo = params.get("periodo");
			String escenario = params.get("escenario");
			String dqf = params.get("DQF");
			String drivername = params.get("drivername");

			String dimfuenteref = params.get("dimfuenteref");
			String modulofuente = params.get("modulofuente");

			String dimdestinoref = params.get("dimdestinoref");
			String modulodestino = params.get("modulodestino");
			String database = driver.getModelNameAsDB(modelo);

			String[] destinoDimMemberRefs = dimdestinoref.split("-");
			String[] fuenteDimMemberRefs = dimfuenteref.split("-");

			sql.append("insert into " + database + ".dbo.asignacionTemporal "
					+ "(" + "  __Activo" + ", _IDPeriodo" + ", _NombreModelo"
					+ ", _IDEscenario" + ", DimensionFuente"
					+ ", FuenteDimMemberRef1" + ", FuenteDimMemberRef2"
					+ ", FuenteDimMemberRef3" + ", FuenteDimMemberRef4"
					+ ", FuenteDimMemberRef5" + ", FuenteDimMemberRef6"
					+ ", FuenteDimMemberRef7" + ", FuenteDimMemberRef8"
					+ ", FuenteDimMemberRef9" + ", FuenteDimMemberRef10"
					+ ", FuenteDimMemberRef11" + ", FuenteDimMemberRef12"
					+ ", _TipoModuloABCFuente" + ", _NombreConductor"
					+ ", DimensionDestino" + ", DestinoDimMemberRef1"
					+ ", DestinoDimMemberRef2" + ", DestinoDimMemberRef3"
					+ ", DestinoDimMemberRef4" + ", DestinoDimMemberRef5"
					+ ", DestinoDimMemberRef6" + ", DestinoDimMemberRef7"
					+ ", DestinoDimMemberRef8" + ", DestinoDimMemberRef9"
					+ ", DestinoDimMemberRef10" + ", DestinoDimMemberRef11"
					+ ", DestinoDimMemberRef12" + ", _TipoModuloABCDestino"
					+ ", DQF )" + "values(" + " 1, " + " '" + periodo + "',  "
					+ " '" + modelo + "',  " + " '" + escenario + "',  " + " '"
					+ dimfuenteref + "',  ");

			// DimMemberRef parsing
			for (int i = 0; i < DIMMEMBER_REFS; i++) {

				if (i < fuenteDimMemberRefs.length)
					sql.append(" '" + fuenteDimMemberRefs[i] + "',  ");
				else
					sql.append(" NULL,   ");
			}

			sql.append(" '" + modulofuente + "', '" + drivername + "', '"
					+ dimdestinoref + "',  ");

			for (int i = 0; i < DIMMEMBER_REFS; i++) {
				if (i < destinoDimMemberRefs.length)
					sql.append(" '" + destinoDimMemberRefs[i] + "',  ");
				else
					sql.append(" NULL,   ");
			}

			sql.append(" '" + modulodestino + "',  '" + dqf + "' )");
			Logger.info(sql.toString());
			success = driver._queryWithoutResult(sql.toString());
			// success = true;

			if (success) {
				map.put("status", 0);
				map.put("data", new HashMap<String, String>());

			} else {
				// Si hay un error
				map.put("status", -1);
				map.put("data", "No se pudo insertar la asignación.");
			}

			response.put("response", map);
			renderJSON(response);

		} else if (operation.equals("update")) {

			String modelo = params.get("modelo");
			String referencia = params.get("referencia");
			String periodo = params.get("periodo").split("/")[0].trim();
			String escenario = params.get("periodo").split("/")[1].trim();

			String modulodestino = params.get("modulodestino");
			String modulofuente = params.get("modulofuente");

			String dimensionfuente = params.get("dimensionfuente");
			String dimensiondestino = params.get("dimensiondestino");

			String conductor = params.get("conductor");

			String dqf = params.get("dqf");
			String bd = driver.getModelNameAsDB(modelo);

			Logger.info("Update");

			sql.append("update " + bd + ".dbo.asignacionTemporal "
					+ " set DQF = '" + dqf + "' " + "where"
					+ " _NombreModelo = '" + modelo + "'"
					+ " AND _IDPeriodo = '" + periodo + "'"
					+ " AND _IDEscenario = '" + escenario + "'"
					+ " AND _TipoModuloABCFuente = '" + modulofuente + "'"
					+ " AND DimensionFuente = '" + dimensionfuente + "'"
					+ " AND _TipoModuloABCDestino = '" + modulodestino + "'"
					+ " AND _NombreConductor = '" + conductor + "'"
					+ " AND DimensionDestino = '" + dimensiondestino + "'");

			Logger.info(sql.toString());
			success = driver._transactionWithoutResult(sql.toString());

			// Si hay resultados o no devuelve filas
			if (success) {

				map.put("status", 0);
				map.put("data", "");

			} else {
				// Si hay un error
				map.put("status", -1);
				map.put("data", "Error");
			}

			HashMap<String, String> updated = new HashMap<String, String>();
			updated.put("referencia", referencia);
			map.put("status", 0);
			map.put("data", updated);
			response.put("response", map);
			renderJSON(response);

		} else if (operation.equals("remove")) {

			String modelo = params.get("modelo");
			String referencia = params.get("referencia");
			String periodo = params.get("periodo").split("/")[0].trim();
			String escenario = params.get("periodo").split("/")[1].trim();

			String modulodestino = params.get("modulodestino");
			String modulofuente = params.get("modulofuente");

			String dimensionfuente = params.get("dimensionfuente");
			String dimensiondestino = params.get("dimensiondestino");
			
			String conductor = params.get("conductor");

			String bd = driver.getModelNameAsDB(modelo);

			Logger.info("Delete");
			sql.append("delete from " + bd + ".dbo.asignacionTemporal "
					+ " where" + " _NombreModelo = '" + modelo + "'"
					+ " AND _IDPeriodo = '" + periodo + "'"
					+ " AND _IDEscenario = '" + escenario + "'"
					+ " AND _TipoModuloABCFuente = '" + modulofuente + "'"
					+ " AND DimensionFuente = '" + dimensionfuente + "'"
					+ " AND _TipoModuloABCDestino = '" + modulodestino + "'"
					+ " AND _NombreConductor = '" + conductor + "'"
					+ " AND DimensionDestino = '" + dimensiondestino + "'");

			Logger.info(sql.toString());
			success = driver._transactionWithoutResult(sql.toString());
			
			// Si hay resultados o no devuelve filas
			if (success) {
				map.put("status", 0);
				HashMap<String, String> deleted = new HashMap<String, String>();
				deleted.put("referencia", referencia);
				map.put("data", deleted);

			} else {
				// Si hay un error
				map.put("status", -1);
				map.put("data", "Error");
			}

			response.put("response", map);
			renderJSON(response);
		}
	}

	/**
	 * Proveedor de datos para de Periodo/Escenario
	 */
	public static void getPeriodoEscenarioData() {

		String modelo = params.get("modelo");
		String modeloID = params.get("modeloID");

		Driver driver = new Driver();

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> response = new HashMap<String, Object>();

		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

		// Usar modelo del dropbox en la interfaz
		if (modelo != null)
			result = driver.getPeriodoEscenarioData(modelo, modeloID);

		map.put("startRows", 0);
		map.put("endRow", 0);

		// De haber o no haber resultados
		if (result != null) {
			map.put("status", 0);
			map.put("totalRows", result.size());
		} else {
			// Ocurrio un error
			map.put("status", -1);
		}

		map.put("data", result);
		response.put("response", map);

		renderJSON(response);
	}

	public static void getModels() {

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> response = new HashMap<String, Object>();

		Driver driver = new Driver();

		// Usar modelo del dropbox en la interfaz
		ArrayList<HashMap<String, String>> result = driver.getModelosData();

		map.put("startRows", 0);
		map.put("endRow", 0);

		// De haber o no haber resultados
		if (result != null) {
			map.put("status", 0);
			map.put("totalRows", result.size());
		} else {
			// Ocurrio un error
			map.put("status", -1);
		}

		map.put("data", result);
		response.put("response", map);

		renderJSON(response);
	}

	public static void getConductores() {

		String modelo = params.get("modelo");
		String escenario = params.get("escenario");
		String periodo = params.get("periodo");

		Driver driver = new Driver();

		String bd = driver.getModelNameAsDB(modelo);

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> response = new HashMap<String, Object>();

		// Usar modelo del dropbox en la interfaz
		ArrayList<HashMap<String, String>> result = driver.getConductoresData(
				modelo, periodo, escenario, bd);

		map.put("startRows", 0);
		map.put("endRow", 0);

		// De haber o no haber resultados
		if (result != null) {
			map.put("status", 0);
			map.put("totalRows", result.size());
		} else {
			// Ocurrio un error
			map.put("status", -1);
		}

		map.put("data", result);
		response.put("response", map);

		renderJSON(response);
	}

	
	static HashMap<String, String> getCurrentDimension(String modelo,
			String dimName, String tipoModulo) {

		Driver driver = new Driver();
		return driver
				._queryWithResult("select Jerarquia_Dimension as indexDim from "
						+ modelo
						+ ".dbo.ordenDimension where _ID = '"
						+ dimName
						+ "' and _TipoModuloABC = '"
						+ tipoModulo
						+ "'");
	}

	static HashMap<String, String> getNodeCurrentDimension(String modelo,
			String idDimension, String tipoModulo) {

		Driver driver = new Driver();

		return driver
				._queryWithResult("select Jerarquia_Dimension as indexDim from "
						+ modelo
						+ ".dbo.ordenDimension where _ID = '"
						+ idDimension
						+ "' and _TipoModuloABC = '"
						+ tipoModulo
						+ "'");
	}

	static HashMap<String, String> getMaxDimension(String modelo,
			String tipoModulo) {

		Driver driver = new Driver();

		return driver
				._queryWithResult("select MAX(Jerarquia_dimension) as Maxdim, _ID from "
						+ modelo
						+ ".dbo.ordenDimension "
						+ " WHERE _TipoModuloABC='"
						+ tipoModulo
						+ "'"
						+ " group by _ID order by MAX(Jerarquia_dimension) DESC");
	}

	static boolean isLeaf(String modelo, String padreID) {

		Driver driver = new Driver();

		return driver._queryWithResult("select * from " + modelo
				+ ".dbo.Vista_miembroDimension where Padre_ID = '" + padreID
				+ "'") == null;
	}

	static boolean runRelacionDimensionJob(String modelo) {

		Jobs job = null;
		boolean isETLSuccess = false;

		// Corremos ETL Calcula elementos de tabla
		// miembrodimension_relaciondimension
		if (modelo.equalsIgnoreCase("tmp_cs"))
			job = new Jobs(DIMENSIONRELACION_JOB_CS);
		else if (modelo.equalsIgnoreCase("tmp_ap"))
			job = new Jobs(DIMENSIONRELACION_JOB_AP);
		else if (modelo.equalsIgnoreCase("tmp_ti"))
			job = new Jobs(DIMENSIONRELACION_JOB_TI);
		else if (modelo.equalsIgnoreCase("tmp_com"))
			job = new Jobs(DIMENSIONRELACION_JOB_COM);
		else if (modelo.equalsIgnoreCase("tmp_red"))
			job = new Jobs(DIMENSIONRELACION_JOB_RED);
		else if (modelo.equalsIgnoreCase("tmp_corp"))
			job = new Jobs(DIMENSIONRELACION_JOB_CORP);

		if (job != null)
			isETLSuccess = job.runDimensionRelacionJob();

		return isETLSuccess;
	}

	public static final char LF = '\n';
	public static final char CR = '\r';

	public static String chop(String str) {
		if (str == null) {
			return null;
		}
		int strLen = str.length();
		if (strLen < 2) {
			return "";
		}
		int lastIdx = strLen - 1;
		String ret = str.substring(0, lastIdx);
		char last = str.charAt(lastIdx);
		if (last == LF) {
			if (ret.charAt(lastIdx - 1) == CR) {
				return ret.substring(0, lastIdx - 1);
			}
		}
		return ret;
	}

}
