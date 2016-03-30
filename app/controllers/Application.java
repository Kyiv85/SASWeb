package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Arrays;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import models.Configuracion;
import models.Encuesta;
import models.EncuestaForm;
import models.PermisoEncuesta;
import models.Rol;
import models.Status;
import models.TipoEncuesta;
import models.User;
import models.estadistico_com;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
//import sun.org.mozilla.javascript.internal.IdScriptableObject;
import utils.Utilities;
import jobs.Jobs;

import com.google.gson.Gson;
import com.google.gwt.json.client.*;

import db.Driver;
import db.UUIDGenerator;

/**
 * Controlador principal de SASWeb
 *
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 *
 */
@With(Secure.class)
public class Application extends Controller {

	/**
	 * Index de la aplicación
	 *
	 * Se realiza la redirección al módulo correspondiente dependiendo del Rol.
	 * Si un usuario posee mas de un rol, se muestra un menú para elegir el rol
	 * principal en esa sesión.
	 *
	 */
	static String idSeparator = ";";
	
	public static void index() {

		String user = Security.connected();
		User usr = User.find("usuario = ? and activo = true ", user)
				.<User> first();

		if (usr != null) {
			if (usr.rol.size() > 1) {

				usr.rolPrincipal = null;
				usr.save();
				session.put("user", usr);
				main();

			} else {
				usr.rolPrincipal = usr.rol.iterator().next();
				usr.save();
				redirectMain();
			}
		}
		redirect("/secure/login");
	}

	/**
	 * Redirección. Necesaria para usuarios con multiples roles.
	 *
	 */
	public static void redirectMain() {

		String user = Security.connected();
		User usr = User.find("usuario = ? and activo = true ", user)
				.<User> first();

		Configuracion conf = Configuracion.findById((long) 1);

		if (usr.rolPrincipal.descripcion.equals("admin")) {

			// Si el modulo está activo o eres el Administrador
			// Administrador
			if (conf.__AdminActivo || usr.administrador) {

				session.put("user", usr);
				Logger.info("Ha ingresado el usuario " + usr
						+ " con el rol de administrador");
				redirect("/SASWeb/gwt-public/adminsas/admin.html");
			} else {

				render("errors/disabled.html");
			}

		} else if (usr.rolPrincipal.descripcion.equals("encuestado")) {

			// Si el modulo está activo o eres el Administrador
			// Encuestado
			if (conf.__EncuestadoActivo || usr.administrador) {

				session.put("user", usr);
				Logger.info("Ha ingresado el usuario " + usr
						+ " con el rol de encuestado");
				Encuestado.encuestado();
			} else {

				render("errors/disabled.html");
			}

		} else if (usr.rolPrincipal.descripcion.equals("funcional")) {

			// Si el modulo está activo o eres el Administrador
			// Funcional
			if (conf.__FuncionalActivo || usr.administrador) {

				session.put("user", usr);
				Logger.info("Ha ingresado el usuario " + usr
						+ " con el rol de funcional");
				funcional();
			} else {

				render("errors/disabled.html");
			}

		} else if (usr.rolPrincipal.descripcion.equals("generador")) {

			// Si el modulo está activo o eres el Administrador
			// Encuestado
			if (conf.__GeneradorActivo || usr.administrador) {

				session.put("user", usr);
				Logger.info("Ha ingresado el usuario " + usr
						+ " con el rol de generador");
				redirect("/SASWeb/admin/");
			} else {

				render("errors/disabled.html");
			}
		} else if (usr.rolPrincipal.descripcion.equals("seguridad")) {

			session.put("user", usr);
			Logger.info("Ha ingresado el usuario " + usr
					+ " con el rol de seguridad");
			redirect("/SASWeb/seguridad/list");
		}
	}

	/**
	 * Elección de roles
	 */
	public static void main() {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de eleccion de roles");

		String usr = Security.connected();
		User user = User.find("byUsuario", usr).<User> first();
		Set<Rol> roles = user.rol;

		render(roles);
	}

	/**
	 * Cambio de rol y redirección
	 */
	public static void changeRole(String id) {

		Logger.info("El usuario " + Security.connected()
				+ " cambio su rol Principal");

		String usr = Security.connected();
		User user = User.find("byUsuario", usr).<User> first();

		Rol principal = Rol.findById(new Long(id));

		if (principal != null) {

			// Si no es un rol forzado que no ha sido asignado
			if (user.rol.contains(principal)) {

				user.rolPrincipal = principal;
				user.save();
				redirectMain();
			}
		}

		main();
	}

	/**
	 * Vista de Administrador SASWebAdmin
	 */
	@Check("admin")
	public static void interceptAdmin(String url) {
		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista Administrador, URI: "
				+ "gwt-public/adminsas/admin.html");
		redirect("/gwt-public/adminsas/admin.html");
	}

	/**
	 * Index de usuario funcional
	 */
	@Check("funcional")
	public static void funcional() {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista Funcional");

		String usr = Security.connected();
		User user = User.find("byUsuario", usr).<User> first();

		// Enviado a Funcional
		Status envi = Status.findById((long) 2);
		// Validado por Funcional
		Status validado = Status.findById((long) 3);
		// Rechazado por Funcional
		Status rechazado = Status.findById((long) 4);

		// Tipo "Encuesta"
		TipoEncuesta tipoEncuesta = TipoEncuesta.findById((long) 2);
		// Ordenes
		TipoEncuesta tipoOrdenes = TipoEncuesta.findById((long) 1);

		List<PermisoEncuesta> noactualizados = PermisoEncuesta
				.find("(status = ? or status = ?) and __Activo = true and " +
						"(encuesta.tipo = ? or encuesta.tipo = ? )",
						envi, rechazado, tipoEncuesta, tipoOrdenes).fetch();

		List<PermisoEncuesta> actualizados = PermisoEncuesta
				.find("status = ? and __Activo = true and " +
						"(encuesta.tipo = ? or encuesta.tipo = ? )",
						validado, tipoEncuesta, tipoOrdenes).fetch();

		render(noactualizados, actualizados);
	}

	/**
	 * Menu de Estadisticos
	 */
	/*@Check("funcional")
	public static void estadisticos() {

		Logger.info("El usuario " + Security.connected()
				+ " accedió a la vista de Estadistico");

		String usr = Security.connected();
		User user = User.find("byUsuario", usr).<User> first();

		// Enviado a Encuestado
		Status enviEncuestado = Status.findById((long) 1);
		// Enviado a Funcional
		Status envi = Status.findById((long) 2);
		// Validado por Funcional
		Status validado = Status.findById((long) 3);
		// Rechazado por Funcional
		Status rechazado = Status.findById((long) 4);

		// Tipo "Encuesta"
		TipoEncuesta tipoEncuesta = TipoEncuesta.findById((long) 2);
		// Ordenes
		TipoEncuesta tipoOrdenes = TipoEncuesta.findById((long) 1);

		List<PermisoEncuesta> actualizados = PermisoEncuesta
				.find("(status = ? or status = ?) and revisor = ? and __Activo = true and" +
						" (encuesta.tipo = ?  or encuesta.tipo = ? ) ",
						envi, rechazado, user, tipoOrdenes, tipoEncuesta).fetch();

		// No Actualizados
		List<PermisoEncuesta> noactualizados = PermisoEncuesta
				/*.find("(status = ? or status = ?) and revisor = ? and __Activo = true and" +
						" (encuesta.tipo = ? or encuesta.tipo = ? ) ",
						enviEncuestado, validado, user, tipoOrdenes, tipoEncuesta).fetch();
				.find("(status = ?) and revisor = ? and __Activo = true and" +
						" (encuesta.tipo = ? or encuesta.tipo = ? ) ",
						validado, user, tipoOrdenes, tipoEncuesta).fetch();

		ArrayList<HashMap<String, String>> noact = getEstadisticoLines(noactualizados, false);
		ArrayList<HashMap<String, String>> act = getEstadisticoLines(noactualizados, true);

		render(noactualizados, actualizados, noact, act);
	}*/


	/**
	 * Menu de Estadisticos
	 *
	 * Actualizado al 01-03-2016
	 */
	@Check("funcional")
	public static void estadisticos() {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de Estadistico");

		Driver driv = new Driver();

		//Actualizados
		ArrayList<HashMap<String, String>> actualizados = driv.getEstadisticos(1);

		// No Actualizados
		ArrayList<HashMap<String, String>> noactualizados = driv.getEstadisticos(0);


		ArrayList<HashMap<String, String>> act = getEstadisticoLines(actualizados);
		ArrayList<HashMap<String, String>> noact = getEstadisticoLines(noactualizados);

		render(noactualizados, actualizados, act, noact);
			
	}




	/**
	 * Lineas de Estadistico
	 */
	/*public static ArrayList<HashMap<String, String>> getEstadisticoLines(List<PermisoEncuesta> listaEncuestas, boolean activo) {


		ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
		ArrayList<HashMap<String, String>> list = null;

		// Todas las encuestas de Funcional		
		Iterator<PermisoEncuesta> listEnc = listaEncuestas.iterator();

		while (listEnc.hasNext()) {
			
			PermisoEncuesta perm = listEnc.next();
			Encuesta encuesta = perm.encuesta;

			try {
				Driver driver = new Driver();

				// Lista de EncuestaForm (preguntas) de cada Encuesta
				List<EncuestaForm> preguntas = encuesta.encuestas;
				Iterator<EncuestaForm> preguntasIte = preguntas.iterator();

				String[] keys = new String[] { "DestinoDimMemberRef1",
						"DestinoDimMemberRef2", "DestinoDimMemberRef3",
						"DestinoDimMemberRef4", "DestinoDimMemberRef5",
						"DestinoDimMemberRef6", "DestinoDimMemberRef7",
						"DestinoDimMemberRef8", "DestinoDimMemberRef9",
						"DestinoDimMemberRef10", "DestinoDimMemberRef11",
						"DestinoDimMemberRef12" };

				// Pido preguntas por cada EncuestaForm
				while (preguntasIte.hasNext()) {

					EncuestaForm tmp = preguntasIte.next();
					
					// Lista de Items
					ArrayList<HashMap<String, String>> result = driver.getFilasEncuesta(perm.modelo, perm.periodo, perm.ceco, tmp.nombreConductor, activo);
					Iterator<HashMap<String, String>> rowList = result
							.iterator();

					while (rowList.hasNext()) {

						HashMap<String, String> tmpRow = rowList.next();
						StringBuilder sb = new StringBuilder();

						for (String s : keys) {

							String dimension = tmpRow.get(s);

							if (!dimension.equals("")) {
								dimension=dimension.replaceAll("\\s+","");
								Logger.info(dimension);
								sb.append(dimension);
								sb.append("-");
							}
						}
						sb.deleteCharAt(sb.length() - 1);
						tmpRow.put("Nombre_Actividad", sb.toString());
						tmpRow.put("perm_id", perm.id.toString());
						tmpRow.put("periodo", perm.periodo);
						tmpRow.put("status", perm.status.toString());
					}
					items.addAll(result);

				}

				// Unir lineas iguales
				list = Utilities.getMergedListEstadistico(items);

				// Termino de obtener items
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}*/



	/**
	 * Lineas de Estadistico
	 * 
	 * Cambios al 01-03-2016
	 * Se maneja a través de la tabla estadistico_com 
	 *
	 */
	public static ArrayList<HashMap<String, String>> getEstadisticoLines(ArrayList<HashMap<String, String>> listaEstadisticos) {

		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();		

		//Traer todos los estadisticos		
		Iterator<HashMap<String, String>> listEst = listaEstadisticos.iterator();

		while (listEst.hasNext()) {
		
			//Traer los valores de los estadisticos	
			try 
			{
				
				Driver driver = new Driver();

				HashMap<String, String> tmp = listEst.next();

				//Logger.info("Valor actual "+tmp.toString());
				
				String region = tmp.get("region");
				String proceso = tmp.get("proceso");
				String unidad = tmp.get("unidad");
				String segmento = tmp.get("segmento");
				String periodo = tmp.get("periodo");
				String modelo = tmp.get("modelo");
				
				// Traer descripciones para cada campo
				HashMap<String, String> desc_seg = driver.getDescMiembDim(segmento, periodo, modelo);
				HashMap<String, String> desc_reg = driver.getDescMiembDim(region, periodo, modelo);
				HashMap<String, String> desc_uni = driver.getDescMiembDim(unidad, periodo, modelo);
				HashMap<String, String> desc_pro = driver.getDescMiembDim(proceso, periodo, modelo);
				
				//Concatenar descripciones en un solo string
				StringBuilder dimension = new StringBuilder();
				dimension.append((desc_seg.get("Nombre")).toString());
				dimension.append("-");
				dimension.append((desc_reg.get("Nombre")).toString());
				dimension.append("-");
				dimension.append((desc_uni.get("Nombre")).toString());
				dimension.append("-");
				dimension.append((desc_pro.get("Nombre")).toString());
				
				dimension.deleteCharAt(dimension.length() - 1);
				tmp.put("Nombre_Actividad", dimension.toString());

				list.add(tmp);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return list;
	}


	/**
	 * Llenar estadistico
	 * Cambio 01-03-2016 para trabajar con tabla estadistico_com
	 */
	@Check("funcional")
	public static void llenarEstadistico(String region, String proceso, String unidad, String segmento) {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de llenar Estadistico");

		Driver driv = new Driver();

		//Traer el estadistico a llenar
		HashMap<String, String> estActual = driv.getEstadisticoActual(region,proceso,unidad,segmento,0);

		//Logger.info(estActual.toString());
		String periodo = estActual.get("periodo");
		String modelo = estActual.get("modelo");

				
		// Traer descripciones para cada campo
		HashMap<String, String> desc_seg = driv.getDescMiembDim(segmento, periodo, modelo);
		HashMap<String, String> desc_reg = driv.getDescMiembDim(region, periodo, modelo);
		HashMap<String, String> desc_uni = driv.getDescMiembDim(unidad, periodo, modelo);
		HashMap<String, String> desc_pro = driv.getDescMiembDim(proceso, periodo, modelo);
			
		//Concatenar descripciones en un solo string
		StringBuilder dimension = new StringBuilder();
		dimension.append((desc_seg.get("Nombre")).toString());
		dimension.append("-");
		dimension.append((desc_reg.get("Nombre")).toString());
		dimension.append("-");
		dimension.append((desc_uni.get("Nombre")).toString());
		dimension.append("-");
		dimension.append((desc_pro.get("Nombre")).toString());
			
		dimension.deleteCharAt(dimension.length() - 1);

		String nombreActividad = dimension.toString();

		//Armar la DimensionFuente
		StringBuilder sb = new StringBuilder();
		sb.append(segmento);
		sb.append("-");
		sb.append(region);
		sb.append("-");
		sb.append(unidad);
		sb.append("-");
		sb.append(proceso);

		String dimFuente = sb.toString();

		Logger.info("Dimension fuente "+dimFuente);

		String escenario = "ACTUAL";

		//Conductor de estadistico
		ArrayList<HashMap<String, String>> conductores = driv.getConductorEst(dimFuente);

		//Clientes
		ArrayList<HashMap<String, String>> clientes = driv.getActividadesDataNEW("TC");

		//Procesos comerciales
		ArrayList<HashMap<String, String>> procesos = driv.getActividadesDataNEW("PC");


		String seg = segmento;
		String reg = region;
		String uni = unidad;
		String pro = proceso;

		render(periodo, modelo, nombreActividad, dimFuente, conductores, clientes, procesos, seg, reg, uni, pro);
		
	}







	/**
	 * Llenar Encuesta Funcional
	 */
	@Check("funcional")
	public static void llenarEncuestaFuncional(int idPermiso, int idEncuesta) {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de llenar Encuesta");

		// Enviar encuesta, encuesta.encuestas(encuestaform),
		ArrayList<HashMap<String, String>> actividades;
		HashMap<String, List> selectBoxes;

		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);
		Encuesta encuesta = Encuesta.findById((long) idEncuesta);

		Driver driv = new Driver();

		// ID, Nombre
		actividades = driv.getActividades(encuesta.modelo, perm.periodo,
				perm.escenario);
		selectBoxes = new HashMap<String, List>();

		for (int i = 0; i < actividades.size(); i++) {

			String dim = actividades.get(i).get("id");
			selectBoxes.put(dim, driv.getActividadesData(dim, encuesta.modelo,
					perm.periodo, perm.escenario));

		}

		render("Encuestado/llenarEncuesta.html", perm, encuesta, actividades,
				selectBoxes);
	}

	/**
	 * Guarda Encuesta
	 */
	@Check("encuestado")
	public static void guardarEncuesta(int id) {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de Guardar Encuesta");
		Encuestado.encuestado();
	}

	/**
	 * Vista de Generador
	 */
	@Check("generador")
	public static void generador() {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de Encuestado");
		List<Encuesta> noactualizados = Encuesta.findAll();

		render(noactualizados);
	}

	/**
	 * Uso en CRUD de PermisoEncuesta
	 */
	public static void showEncuestaData(int id) {

		Encuesta encuesta = Encuesta.findById((long) id);

		Driver driv = new Driver();

		//ArrayList<HashMap<String, String>> cond = 
		//	                    driv.getConductor(encuesta.modelo);
		
		int last = driv.getLastPosicion(id);

		ArrayList<HashMap<String, String>> cond = 
			                    driv.getConductorEncuesta(id,encuesta.modelo);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("modelo", encuesta.modelo);
		map.put("tipo", encuesta.tipo.descripcion);
		map.put("conductor", Utilities.getArrayMap(cond, "Nombre", "ID"));
		//map.put("preguntas", Utilities.getArrayMap(preg, "Nombre", "ID"));
		map.put("posicion", last);

		renderJSON(map);
	}

	/**
	 * Uso en CRUD de PermisoEncuesta. Lista Datos de filas de encuesta
	 */
	public static void listEncuestaRowData(String idPermiso, String idPregunta) {

		int i = 0;
		float dqfTotal = 0;

		String periodo = "";
		String ceco = "";
		String conductor = "";
		String modelo = "";
		int posicion = 0;

		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> userdata = new HashMap<String, Object>();

		Driver driver = new Driver();

		try {
			
			PermisoEncuesta permEncuesta = PermisoEncuesta.findById(new Long(idPermiso));
			EncuestaForm pregunta = EncuestaForm.findById(new Long(idPregunta));
			
			periodo = permEncuesta.periodo;
			ceco = permEncuesta.ceco;
			modelo = permEncuesta.modelo;
			conductor = pregunta.nombreConductor;
			posicion = pregunta.posicion;
			
			ArrayList<HashMap<String, String>> result = 
                                      driver.getFilasEncuesta(modelo, periodo, ceco, conductor, false);

			ArrayList<HashMap<String, Object>> rows = new ArrayList<HashMap<String, Object>>();
			Iterator<HashMap<String, String>> it = result.iterator();

			while (it.hasNext()) {

				HashMap<String, String> tmp = it.next();
				HashMap<String, Object> row = new HashMap<String, Object>();

				row.put("id", periodo+ idSeparator + ceco + idSeparator + conductor +  idSeparator +tmp.get("DimensionDestino"));
				row.put("cell", tmp);
				rows.add(row);
				i++;
			}

			HashMap<String, String> dqfSum = 
				            driver.getDQFSum(modelo, periodo, ceco, conductor);

			if (!dqfSum.get("Total").equals("")) {
				dqfTotal = Float.parseFloat(dqfSum.get("Total")); 
			}

			userdata.put("cell.DestinoDimMemberRef1", "Total:");
			userdata.put("cell.DQF", dqfTotal);

			map.put("total", 1);
			map.put("pages", 1);
			map.put("records", result.size());

			map.put("rows", rows);
			map.put("userdata", userdata);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		renderJSON(map);
	}

	/**
	 * Lista filas de datos de Estadistico
	 */
	public static void listEncuestaRowDataEstadistico(String region, String proceso, String unidad, String segmento) {

		int i = 0;
		float dqfTotal = 0;
		
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<HashMap<String, Object>> rows = new ArrayList<HashMap<String, Object>>();
		Map<String, Object> userdata = new HashMap<String, Object>();
		
		Driver driv = new Driver();

		//Traer el estadistico a llenar
		HashMap<String, String> estActual = driv.getEstadisticoActual(region,proceso,unidad,segmento,0);

		Logger.info(estActual.toString());
		String periodo = estActual.get("periodo");
		String modelo = estActual.get("modelo");

		//Armar la DimensionFuente
		StringBuilder sb = new StringBuilder();
		sb.append(segmento);
		sb.append("-");
		sb.append(region);
		sb.append("-");
		sb.append(unidad);
		sb.append("-");
		sb.append(proceso);

		//sb.deleteCharAt(sb.length() - 1);

		String dimFuente = sb.toString();

		try {

			ArrayList<HashMap<String, String>> result = driv.getEstadisticoRowDataSingles(dimFuente);

			Iterator<HashMap<String, String>> it = result.iterator();
			
			while (it.hasNext()) {

				HashMap<String, String> tmp = it.next();
				HashMap<String, Object> row = new HashMap<String, Object>();

				row.put("id", tmp.get("DimensionDestino"));
				row.put("cell", tmp);
				rows.add(row);
				i++;
			}

			HashMap<String, String> dqfSum = 
				            driv.getDQFSumEstadistico(dimFuente);

			if (!dqfSum.get("Total").equals("")) {
				dqfTotal = Float.parseFloat(dqfSum.get("Total")); 
			}

			userdata.put("cell.DestinoDimMemberRef2", "Total:");
			userdata.put("cell.DQF", dqfTotal);

			map.put("total", 1);
			map.put("pages", 1);
			map.put("records", result.size());

			map.put("rows", rows);
			map.put("userdata", userdata);

			
		} catch (Exception e) {
			e.printStackTrace();
		}

		renderJSON(map);
	}

	/**
	 * Inserta filas de datos de Estadistico
	 */
	public static void insertEncuestaRowDataEstadistico(int idPermiso,
			int idPregunta) {

		String activo = "0";
		String tipoModuloABCFuente = "ACTIVITY";
		String tipoModuloABCDestino = "COSTOBJECT";
		String dimensionDestino = "";
		String DQF;
		StringBuffer dimValues = new StringBuffer();

		boolean success = false;
		
		ArrayList<String> dimDestino = new ArrayList<String>();

		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);

		// Obtener parametros
		String modelo = perm.modelo;
		String periodo = perm.periodo;
		String escenario = perm.escenario;

		String dimFuente = params.get("dimFuente");
		
		Driver driv = new Driver();
		Gson gson = new Gson();

		EncuestaForm encuestaPreg = EncuestaForm.findById((long) idPregunta);
		String conductor = encuestaPreg.nombreConductor;

		// Dimension Fuente
		String choices = params.get("choices", String.class);
		Data[] decoded = gson.fromJson(choices, Data[].class);

		// Tomar toda la data menos la Medida
		for (int i = 0; i < decoded.length - 1; i++) {
			dimDestino.add(decoded[i].value);

			if (dimensionDestino.equals("")) {
				dimensionDestino = decoded[i].value;
			} else {
				dimensionDestino = dimensionDestino + "-" + decoded[i].value;
			}
		}

		for (int i = 0; i < 12; i++) {
			String value;
			try {
				value = dimDestino.get(i);
				dimValues.append("'" + value + "',");

			} catch (IndexOutOfBoundsException e) {
				dimValues.append("null,");
			}
		}

		// DQF
		DQF = decoded[decoded.length - 1].value;

		String bd = Driver.getModelNameAsDB(modelo);

		HashMap<String, Object> map = new HashMap<String, Object>();
		
		StringBuffer dimFuenteValues = new StringBuffer();
		String [] dimFuenteParsed = dimFuente.split("-");
		
		// Parseo de dimFuente
		for (int i = 0; i < 12; i++) {
			String value;
			try {
				
				value = dimFuenteParsed[i];
				dimFuenteValues.append("'" + value + "',");

			} catch (IndexOutOfBoundsException e) {
				dimFuenteValues.append("null,");
			}
		}

		String sql = "INSERT INTO "
				+ bd
				+ ".AsignacionTemporal ( "
				+ "__Activo, _IDPeriodo, _NombreModelo, _IDEscenario, DimensionFuente, "
				+ "FuenteDimMemberRef1, FuenteDimMemberRef2, FuenteDimMemberRef3,  FuenteDimMemberRef4,  FuenteDimMemberRef5, "
				+ "FuenteDimMemberRef6, FuenteDimMemberRef7, FuenteDimMemberRef8,  FuenteDimMemberRef9,  FuenteDimMemberRef10, "
				+ "FuenteDimMemberRef11, FuenteDimMemberRef12, "
				+ "_TipoModuloABCFuente, _NombreConductor, DimensionDestino, "
				+ "DestinoDimMemberRef1, DestinoDimMemberRef2,DestinoDimMemberRef3,DestinoDimMemberRef4,DestinoDimMemberRef5,"
				+ "DestinoDimMemberRef6,DestinoDimMemberRef7,DestinoDimMemberRef8,DestinoDimMemberRef9, DestinoDimMemberRef10, "
				+ "DestinoDimMemberRef11, DestinoDimMemberRef12, "
				+ "_TipoModuloABCDestino, DQF, _idPermisoEncuesta, _idPregunta,  fechaAsignacion)"
				+ "values (" + "'" + activo + "', " + "'" + periodo + "', "
				+ "'" + modelo + "', " + "'" + escenario + "', " + "'"
				+ dimFuente + "', " + dimFuenteValues.toString() + "'"
				+ tipoModuloABCFuente + "', " + "'" + conductor + "', " + "'"
				+ dimensionDestino + "', " + dimValues.toString() + "'"
				+ tipoModuloABCDestino + "', " + "'" + DQF + "', '" + idPermiso
				//+ "', '" + idPregunta + "', '" + Utilities.getTimeStamp()+"')";//error en timestamp entre formato BBDD y CONTROLLER
				+ "', '" + idPregunta + "', NOW())";
		
		
		Logger.info(sql);
		success = driv._queryWithoutResult(sql);

		if (success) {
			map.put("success", "true");
		} else {
			map.put("success", "false");
			map.put("message",
					"Asignación ya existe. Por favor seleccionar otras combinación");
		}

		renderJSON(map);
	}


	//Inserta valor estadistico para comercial 03-03-2016
	public static void insertEncuestaRowDataEstadisticos(String periodo, String segmento, String region, String unidad, String proceso, String valdqf){
		Logger.info("Entrando en insertar valor de estadístico");
		boolean success = false;

		//Armar la DimensionFuente
		StringBuilder sb = new StringBuilder();
		sb.append(segmento);
		sb.append("-");
		sb.append(region);
		sb.append("-");
		sb.append(unidad);
		sb.append("-");
		sb.append(proceso);

		//sb.deleteCharAt(sb.length() - 1);

		String dimFuente = sb.toString();
		
		String data[] = params.get("choices").split("%3B");
		String datas[] = data[0].replaceAll("^\\{|\\}$","").split("\"?(:|,)(?![^\\{]*\\})\"?");
		
		String part[];
		String parts[];

		List<String> parts2 = new ArrayList<String>();

		for (int i=0;i<datas.length;i++ ) {
			//Logger.info("Parte datas "+i+": "+datas[i]);
			part = datas[i].split(",");
			for(int j=0;j<part.length;j++ ){
				//Logger.info("Parte part "+j+": "+part[j]);
				parts = part[j].split(":");
				if(j==1){
					for(int k=0;k<parts.length;k++ ){
				  	 	if(k==1){
							//Logger.info("Parte parts "+k+": "+part[k]);
							parts2.add(part[k]);
				     	}
					}
				}
			}			
		}

		String conductor = "";
		String cliente = "";
		String comercial = "";

		for (int i=0;i<parts2.size();i++ ) {
			Logger.info("Parte parts2 "+i+": "+parts2.get(i));
			switch (i) {
            case 0:  conductor = parts2.get(i);
                     break;
            case 1:  cliente = parts2.get(i);
                     break;
            case 2:  comercial = parts2.get(i);
                     break;
            }
		}

		conductor = conductor.substring(9,conductor.length()-2);
		cliente = cliente.substring(9,cliente.length()-2);
		comercial = comercial.substring(9,comercial.length()-3);		
		
		double valorDQF = Double.parseDouble(valdqf);

		//Armar Dimension Destino
		StringBuilder sbr = new StringBuilder();
		sbr.append(cliente);
		sbr.append("-");
		sbr.append(comercial);

		String dimDestino = sbr.toString();
		
		HashMap<String, Object> map = new HashMap<String, Object>();

		Driver driv = new Driver();
		
		String sql = "INSERT INTO TMP_COM.AsignacionTemporal ( __Activo, _IDPeriodo, _NombreModelo," 
				+ " _IDEscenario, DimensionFuente, FuenteDimMemberRef1, FuenteDimMemberRef2, FuenteDimMemberRef3,"
				+ " FuenteDimMemberRef4, _TipoModuloABCFuente, _NombreConductor, DimensionDestino," 
				+ " DestinoDimMemberRef1, DestinoDimMemberRef2, _TipoModuloABCDestino, DQF ) VALUES (0,"
				+ " '"+periodo+"','Modelo_COM','ACTUAL','"+dimFuente+"','"+segmento+"','"+region+"','"+unidad+"',"
				+ " '"+proceso+"','ACTIVITY','"+conductor+"','"+dimDestino+"','"+cliente+"','"+comercial+"',"
				+ " 'COSTOBJECT',"+valorDQF+")";
				
		Logger.info(sql);
		
		success = driv._queryWithoutResult(sql);

		if (success) {
			map.put("success", "true");
		} else {
			map.put("success", "false");
			map.put("message",
					"Asignación ya existe. Por favor seleccionar otras combinación");
		}

		renderJSON(map);
	}



	/**
	 * Inserta filas en encuesta
	 */
	public static void insertEncuestaRowData(int idPermiso, int idPregunta, String valorDQF) {

		Logger.info("Application.insertEncuestaRowData");

		String activo = "0";
		//String DQF;

		double dqfval = Double.parseDouble(valorDQF);
		Logger.info("Valor dqf nuevo: "+dqfval);

		Driver driv = new Driver();

		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);

		EncuestaForm encuestaPreg = EncuestaForm.findById((long) idPregunta);
		String conductor = encuestaPreg.nombreConductor;

		// Obtener parametros
		String modelo = perm.modelo;
		String periodo = perm.periodo;
		String escenario = perm.escenario;
		String ceco = perm.ceco;

		StringBuffer dimValues = new StringBuffer();

		// Dimension Fuente
		String tipoModuloABCFuente = "RESOURCE";
		ArrayList<HashMap<String, String>> filiales = driv.getFilialNew(ceco, modelo);

		ArrayList<HashMap<String, String>> ctas = driv.getCuentasNew(conductor,	modelo, ceco);

		// Si no existe conductor asociado
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (ctas.size() == 0) {
			map.put("success", "false");
			map.put("message",
					"No existe conductor asociado. Por favor seleccionar otra combinación");
			renderJSON(map);
		}

		//String cta = ctas.get(0).get("GrupoCuenta");

		String choices = params.get("choices", String.class);

		Gson gson = new Gson();
		Data[] decoded = gson.fromJson(choices, Data[].class);
		
		// Get Dimension Destino
		String tipoModuloABCDestino = "ACTIVITY";
		ArrayList<String> dimDestino = new ArrayList<String>();
		String dimensionDestino = "";

		// Tomar toda la data menos la Medida
		for (int i = 0; i < decoded.length - 1; i++) {
			dimDestino.add(decoded[i].value);
			if (dimensionDestino.equals("")) {
				dimensionDestino = decoded[i].value;
			} else {
				dimensionDestino = dimensionDestino + "-" + decoded[i].value;
			}
		}

		for (int i = 0; i < 12; i++) {
			String value;
			try {
				value = dimDestino.get(i);
				dimValues.append("'" + value + "',");

			} catch (IndexOutOfBoundsException e) {
				dimValues.append("null,");
			}
		}

		// DQF
		//DQF = decoded[decoded.length - 1].value;
		String filial = null;
		String sql = null;
		String DimensionFuente = null;
		String bd = Driver.getModelNameAsDB(modelo);

		boolean success = false;

		String idInsert = UUIDGenerator.getUUID();

		// Si el tipoValor es un porcentaje
		if (encuestaPreg.tipoValor.nombre.equals("Porcentaje")) {
			
			// Obtener total de DQF de una pregunta
			HashMap<String, String> dqfSum = driv.getDQFSum(modelo, periodo, ceco, conductor);

			if (!dqfSum.get("Total").equals("")) {

				float dqfTotal = Float.parseFloat(dqfSum.get("Total"));
				//int totalAgregado = (int) dqfTotal + Integer.parseInt(DQF);
				int totalAgregado = (int) dqfTotal + (int) dqfval;
				// lo que llevamos mas el nuevo es mayor a 100
				// Se debe tomar en cuenta la cantidad de filiales
				if (totalAgregado > 100) {

					map.put("success", "false");
					map.put("message", "El total no puede ser mayor a 100%");
					renderJSON(map);
				}
				
			}

		}

		//La inserción se realiza según cuantas filiales y grupos de cuenta existan
		for (int i = 0; i < filiales.size(); i++) {
		 for (int j = 0; j < ctas.size(); j++) {
		 		
		 		

			filial = filiales.get(i).get("ID");
			String cta = ctas.get(j).get("GrupoCuenta");
			DimensionFuente = filial + "-" + ceco + "-" + cta;



			sql = "INSERT INTO "
					+ bd
					+ ".AsignacionTemporal ( "
					+ "__Activo, _IDPeriodo, _NombreModelo, _IDEscenario, DimensionFuente, "
					+ "FuenteDimMemberRef1, FuenteDimMemberRef2, FuenteDimMemberRef3, "
					+ "_TipoModuloABCFuente, _NombreConductor, DimensionDestino, "
					+ "DestinoDimMemberRef1, DestinoDimMemberRef2,DestinoDimMemberRef3,DestinoDimMemberRef4,DestinoDimMemberRef5,"
					+ "DestinoDimMemberRef6,DestinoDimMemberRef7,DestinoDimMemberRef8,DestinoDimMemberRef9, DestinoDimMemberRef10, "
					+ "DestinoDimMemberRef11, DestinoDimMemberRef12, "
					+ "_TipoModuloABCDestino, DQF, _idPermisoEncuesta, _idPregunta,  fechaAsignacion, _idInsert )"
					+ " values (" + "'" + activo + "', " + "'" + periodo + "', "
					+ "'" + modelo + "', " + "'" + escenario + "', " + "'"
					+ DimensionFuente + "', " + "'" + filial + "', " + "'"
					+ ceco + "', " + "'" + cta + "', " + "'"
					+ tipoModuloABCFuente + "', " + "'" + conductor + "', "
					+ "'" + dimensionDestino + "', " + dimValues.toString()
					+ "'" + tipoModuloABCDestino + "', " + "'" + valorDQF + "', '"
					+ idPermiso + "', '" + idPregunta + "',"
					//+ Utilities.getTimeStamp() + "' , '" + idInsert + "') ;"; //error en timestamp entre formato BBDD y CONTROLLER
                    + " NOW() ,'" + idInsert + "')";
			Logger.info(sql) ;
			success = driv._queryWithoutResult(sql);

			if (!success)
				break;
		 }
		}

		
		if (success) {
			map.put("success", "true");
		} else {
			map.put("success", "false");
			map.put("message",
					"Asignación ya existe. Por favor seleccionar otra combinación");
		}
		
		renderJSON(map);
	}

	/**
	 * Modifica filas en encuesta
	 * Req
	 * EP 18032015
	 * ACTUALIZADO NUEVAMENTE AL 04-02-2016
	 */
	public static void actualizaEncuestaRowData(int idPermiso, int idPregunta, String conductor, String valorDQF) {

		boolean success = false;
		String sql = null;
		
		/*
		String data[] = params.get("choices").split("%3B");
		String datas[] = data[0].split(";");
		String datas2[] = datas[3].split(",");
		Logger.info("variable data " + data[0] + ".");
		Logger.info("variable datas " + datas[2] + ".");
		Logger.info("variable datas2 " + datas2[0] + ".");
		String periodo = datas[0];
		periodo = periodo.substring(7);
		String ceco = params.get("ceco");
		String dqfn = params.get("dqf");
		Logger.info("variable periodo " + periodo + ".");
		Logger.info("variable ceco " + ceco + ".");
		Logger.info("variable ceco " + dqfn + ".");

		String conductor = datas[2];
		String dimensionDestino = datas2[0];
		dimensionDestino = dimensionDestino.substring(0,dimensionDestino.length()-1);
		Logger.info("variable data conductor " + conductor + ".");
		Logger.info("variable data dimensiondestino " + dimensionDestino + ".");
		*/


		String data[] = params.get("choices").split("%3B");
		String datas[] = data[0].split(";");
		String datas2[] = datas[3].split(",");
		String dimensionDestino = datas2[0];
		Logger.info("Valor de dimensionDestino antes: "+dimensionDestino);
		dimensionDestino = dimensionDestino.substring(0,dimensionDestino.length()-2);
		Logger.info("Valor de dimensionDestino despues: "+dimensionDestino);

		double dqfval = Double.parseDouble(valorDQF);
		Logger.info("Valor dqf nuevo: "+dqfval);
		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);
	
	    Logger.info("El usuario " + Security.connected()
	            + " accedio a actualizaEncuestaRowData");
	
		Driver driv = new Driver();
	
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		String modelo = perm.modelo;
		String periodo = perm.periodo;
		String ceco = perm.ceco;
		String bd = driv.getModelNameAsDB(modelo);

		sql = "UPDATE " + bd + ".AsignacionTemporal" + " SET "
			+ "DQF = '" + dqfval
		 	+ "' WHERE _IDPeriodo = '" + periodo + "' "
		 	+ " AND FuenteDimMemberRef2 = '" + ceco + "' "
		 	+ " AND FuenteDimMemberRef3 in  " +
		    		"(SELECT distinct grupoCuenta from " + bd + ".EquivalenciaGC " +
		    	      " WHERE _IDConductor = '" + conductor + "') "
		    + " AND _NombreConductor = '" + conductor + "' "
		    + " AND DimensionDestino = '" + dimensionDestino + "' ";
		Logger.info(sql);
		success = driv._queryWithoutResult(sql);
	
		if (success)
			map.put("success", "true");
		else
			map.put("success", "false");
	
		renderJSON(map);
	}
	
	/**
	 * Modifica filas en encuesta
	 * Req
	 * EP 18032015
	 *
	 *
	 * Nuevo cambio al 01-03-2016
	 */
	public static void actualizaEncuestaRowDataEst(String region, String proceso, String unidad, String segmento, String valdqf) {

		Logger.info("Entrando en actualizaEncuestaRowDataEst");

		boolean success = false;
		String sql = null;
		HashMap<String, Object> map = new HashMap<String, Object>();

		ArrayList<HashMap<String, Object>> rows = new ArrayList<HashMap<String, Object>>();
		Map<String, Object> userdata = new HashMap<String, Object>();
		
		Driver driv = new Driver();

		//Traer el estadistico a llenar
		HashMap<String, String> estActual = driv.getEstadisticoActual(region,proceso,unidad,segmento,0);

		Logger.info(estActual.toString());
		String periodo = estActual.get("periodo");

		//Armar la DimensionFuente
		StringBuilder sb = new StringBuilder();
		sb.append(segmento);
		sb.append("-");
		sb.append(region);
		sb.append("-");
		sb.append(unidad);
		sb.append("-");
		sb.append(proceso);

		String dimFuente = sb.toString();

		String data[] = params.get("choices").split("%3B");
		String datas[] = data[0].replaceAll("^\\{|\\}$","").split("\"?(:|,)(?![^\\{]*\\})\"?");

		String conductor = datas[3];
		String cli = datas[5];
		String cliente = driv.getIdMiembroDimension(cli,"TC");
		String com = datas[7];
		String comercial = driv.getIdMiembroDimension(com,"PC");

		//Armar Dimension Destino
		StringBuilder sbr = new StringBuilder();
		sbr.append(cliente);
		sbr.append("-");
		sbr.append(comercial);

		String dimDestino = sbr.toString();

		double valorDQF = Double.parseDouble(valdqf);

		sql = "UPDATE TMP_COM.AsignacionTemporal SET "
			+ " DQF = " + valorDQF
		 	+ " WHERE _IDPeriodo = '" + periodo + "' "
		    + " AND DimensionFuente = '" + dimFuente + "' "
		    + " AND _NombreConductor = '" + conductor + "' "
		    + " AND DimensionDestino = '" + dimDestino + "' "
		    + " AND _TipoModuloABCFuente = 'ACTIVITY' "
			+ " AND _TipoModuloABCDestino = 'COSTOBJECT'";
		
		Logger.info(sql);
		success = driv._queryWithoutResult(sql);

		if (success)
			map.put("success", "true");
		else
			map.put("success", "false");
	
		renderJSON(map);
	}

	
	/**
	 * Borra filas en encuesta
	 * Usado para un solo ítem al 04-02-2016
	 * 
	 */
	public static void deleteEncuestaRowData(int idPermiso, int idPregunta) {
	
		boolean success = false;
		String sql = null;
		
		String data[] = params.get("id").split(idSeparator);
		
		String periodo = data[0];
		String ceco = data[1];
		String conductor = data[2];
		String dimensionDestino = data[3];
	
		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);
	
	    Logger.info("El usuario " + Security.connected()
	            + " accedio a deleteEncuestaRowData");
	
		Driver driv = new Driver();
	
		HashMap<String, Object> map = new HashMap<String, Object>();
	
		String modelo = perm.modelo;
		String bd = Driver.getModelNameAsDB(modelo);

		sql = "DELETE FROM " + bd + ".AsignacionTemporal" + " WHERE "
		 	+ "_IDPeriodo = '" + periodo + "' "
		    + " AND FuenteDimMemberRef2 = '" + ceco + "' "
		    + " AND _NombreConductor = '" + conductor + "' "
		    + " AND DimensionDestino = '" + dimensionDestino + "' ";

		Logger.info(sql);
		
		success = driv._queryWithoutResult(sql);
	
		if (success)
			map.put("success", "true");
		else
			map.put("success", "false");
	
		renderJSON(map);
	}




	/**
	 * Borra filas en encuesta
	 * Usado para varios ítems al 04-02-2016
	 * 
	 */
	public static void deleteEncuestaRowDatass(int idPermiso, int idPregunta) {
		
		Logger.info("El usuario " + Security.connected()
	            + " accedio a deleteEncuestaRowDatass");

		boolean success = false;
		String sql = null;
		
		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);
	
		Driver driv = new Driver();
	
		HashMap<String, Object> map = new HashMap<String, Object>();
	
		String modelo = perm.modelo;
		String bd = Driver.getModelNameAsDB(modelo);


		String data1[] = params.get("choices").split(",");
		Logger.info("items entrantes "+data1.length);

		int i = 0;
		StringTokenizer st = new StringTokenizer(data1[0]);
		if(st.countTokens() < 2)
			i=1;
	
		Logger.info("Valor de i antes del while "+i);		
		while(i < data1.length){


			Logger.info("Data entrante "+data1[i]);

			String data2[] = data1[i].split(";");
			
			String periodo;

			if( i == 0)
				periodo = data2[0].substring(2,data2[0].length());
			else
				periodo = data2[0].substring(1,data2[0].length());
			
			String ceco = data2[1];
			String conductor = data2[2];
			String dimensionDestino;

			if(i == (data1.length-1))
				dimensionDestino = data2[3].substring(0,data2[3].length()-2);
			else
				dimensionDestino = data2[3].substring(0,data2[3].length()-1); 
			

			sql = "DELETE FROM " + bd + ".AsignacionTemporal" + " WHERE "
		 			+ "_IDPeriodo = '" + periodo + "' "
		    		+ " AND FuenteDimMemberRef2 = '" + ceco + "' "
		    		+ " AND _NombreConductor = '" + conductor + "' "
		    		+ " AND DimensionDestino = '" + dimensionDestino + "' ";
			
			Logger.info(sql);
			
			success = driv._queryWithoutResult(sql);

			if (!success)
				break;
			
			i++;
		}

		if (success)
			map.put("success", "true");
		else
			map.put("success", "false");
	
		renderJSON(map);
	}




	/**
     * Borra filas en encuesta EP
     */
    public static void deleteEncuestaRowDatas(int idPermiso, int idPregunta) {

        boolean success = false;
        String sql = null;
        String dimensionDestino = "";

        String data[] = params.get("id").split(",");
        String datas[] = data[0].split(idSeparator);

        String periodo = datas[0];
        String ceco = datas[1];
        String conductor = datas[2];
        int i;

        for (i = 0; i < data.length; i++) {

            String datax[] = data[i].split(idSeparator);
            if (dimensionDestino.equals("")) {
                dimensionDestino = "'"+datax[3]+"'";
            } else {
                dimensionDestino = dimensionDestino +",'"+datax[3]+"'";
            }
            }


        //dimensionDestino = datas[3];

        PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);

        Logger.info("El usuario " + Security.connected()
                + " accedio a deleteEncuestaRowData con dimensiondestino "+dimensionDestino);

        Driver driv = new Driver();

        HashMap<String, Object> map = new HashMap<String, Object>();

        String modelo = perm.modelo;
        String bd = Driver.getModelNameAsDB(modelo);

        sql = "DELETE FROM " + bd + ".AsignacionTemporal" + " WHERE "
                + "_IDPeriodo = '" + periodo + "' "
                + " AND FuenteDimMemberRef2 = '" + ceco + "' "
                + " AND FuenteDimMemberRef3 in  " +
                "(SELECT distinct grupoCuenta from " + bd + ".EquivalenciaGC " +
                " WHERE _IDConductor = '" + conductor + "') "
                + " AND _NombreConductor = '" + conductor + "' "
                + " AND DimensionDestino in (" + dimensionDestino + ") ";

        Logger.info(sql);
        success = driv._queryWithoutResult(sql);

        if (success)
            map.put("success", "true");
        else
            map.put("success", "false");

        renderJSON(map);
    }



	/**
	 * Borra filas en estadístico
	 *
	 * Aplicado al 01-03-2016
	 */
	public static void deleteEncuestaRowDataEst(String segmento, String region, String unidad, String proceso) {

		Logger.info("Entrando en deleteEncuestaRowDataEst");

		boolean success = false;
		String sql = null;
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		Driver driv = new Driver();

		//Traer el estadistico
		HashMap<String, String> estActual = driv.getEstadisticoActual(region,proceso,unidad,segmento,0);

		Logger.info(estActual.toString());
		String periodo = estActual.get("periodo");

		//Armar la DimensionFuente
		StringBuilder sb = new StringBuilder();
		sb.append(segmento);
		sb.append("-");
		sb.append(region);
		sb.append("-");
		sb.append(unidad);
		sb.append("-");
		sb.append(proceso);

		String dimFuente = sb.toString();

		String data[] = params.get("choices").split("%3B");
		String datas[] = data[0].replaceAll("^\\{|\\}$","").split("\"?(:|,)(?![^\\{]*\\})\"?");

		//Traer clave de clientes y productos comerciales
		String conductor = datas[3];
		String cli = datas[5];
		String cliente = driv.getIdMiembroDimension(cli,"TC");
		String com = datas[7];
		String comercial = driv.getIdMiembroDimension(com,"PC");

		//Armar Dimension Destino
		StringBuilder sbr = new StringBuilder();
		sbr.append(cliente);
		sbr.append("-");
		sbr.append(comercial);

		String dimDestino = sbr.toString();
	
		sql = "DELETE FROM TMP_COM.AsignacionTemporal"
		 	+ " WHERE _IDPeriodo = '" + periodo + "' "
		    + " AND DimensionFuente = '" + dimFuente + "' "
		    + " AND _NombreConductor = '" + conductor + "' "
		    + " AND DimensionDestino = '" + dimDestino + "' "
		    + " AND _TipoModuloABCFuente = 'ACTIVITY' "
			+ " AND _TipoModuloABCDestino = 'COSTOBJECT'";
		    
		Logger.info(sql);
		success = driv._queryWithoutResult(sql);
		
		if (success)
			map.put("success", "true");
		else
			map.put("success", "false");

		renderJSON(map);
	}


	public static void deleteEncuestaRowDataEsts(String segmento, String region, String unidad, String proceso) {

		Logger.info("Entrando en deleteEncuestaRowDataEsts");

		boolean success = false;
		String sql = null;
		HashMap<String, Object> map = new HashMap<String, Object>();

		Driver driv = new Driver();

		//Traer el estadistico
		HashMap<String, String> estActual = driv.getEstadisticoActual(region,proceso,unidad,segmento,0);

		Logger.info(estActual.toString());
		String periodo = estActual.get("periodo");

		//Armar la DimensionFuente
		StringBuilder sb = new StringBuilder();
		sb.append(segmento);
		sb.append("-");
		sb.append(region);
		sb.append("-");
		sb.append(unidad);
		sb.append("-");
		sb.append(proceso);

		String dimFuente = sb.toString();

		String data[] = params.get("choices").split("%3B");
		String datas[] = data[0].replaceAll("^\\{|\\}$","").split("\"?(:|,)(?![^\\{]*\\})\"?");
		String data2[] = null;
                int acum=0;
                String conductor = "";
                String cli = "";
                String com = "";

		for (int i=0; i<datas.length; i++) {
                    Logger.info("Datas "+i+": "+datas[i]);
                    data2 = datas[i].replaceAll("^\\{|\\}$","").split("\"?(:|,)(?![^\\{]*\\})\"?");

                    for (int j=0; j<data2.length; j++) {
                        Logger.info("Data2 "+j+": "+data2[j]);
                    }

                    if(data2.length<10){
                        String datas2[] = data2[0].split(":");
                        for(int k=0; k<datas2.length; k++){
                            Logger.info("DAtas2 "+k+": "+datas2[k]);
                            String datas21[] = datas2[2].split(",");
                            conductor = datas21[0].substring(1, (datas21[0].length())-1);
                            Logger.info("Conductor "+conductor);
                            String datas22[] = datas2[3].split(",");
                            cli = datas22[0].substring(1, (datas22[0].length())-1);
                            Logger.info("Cliente: "+cli);
                            String datas23[] = datas2[4].split(",");
                            com = datas23[0].substring(1, (datas23[0].length())-1);
                            Logger.info("Comercial: "+com);
                        }
                    }
                    else{
                        conductor = data2[3];
                        cli = data2[5];
                        com = data2[7];
                    }
                    //Traer clave de clientes y productos comerciales
                    String cliente = driv.getIdMiembroDimension(cli,"TC");
                    String comercial = driv.getIdMiembroDimension(com,"PC");

                    //Armar Dimension Destino
                    StringBuilder sbr = new StringBuilder();
                    sbr.append(cliente);
                    sbr.append("-");
                    sbr.append(comercial);

                    String dimDestino = sbr.toString();

                    sql = "DELETE FROM TMP_COM.AsignacionTemporal"
                            + " WHERE _IDPeriodo = '" + periodo + "' "
                            + " AND DimensionFuente = '" + dimFuente + "' "
                            + " AND _NombreConductor = '" + conductor + "' "
                            + " AND DimensionDestino = '" + dimDestino + "' "
                            + " AND _TipoModuloABCFuente = 'ACTIVITY' "
                            + " AND _TipoModuloABCDestino = 'COSTOBJECT'";
                    
                    Logger.info(sql);
                    success = driv._queryWithoutResult(sql);
                        
                    if(success)
                        acum++;
                    
		}

		if (acum==datas.length)
                    map.put("success", "true");
		else
                    map.put("success", "false");

		renderJSON(map);
	}
	
	
	/**
	 * Uso en CRUD
	 */
	public static void showPermisoData(String modelo) {

		Driver driver = new Driver();

		// Estadistico
		TipoEncuesta tipo = TipoEncuesta.findById((long) 3);

		Map<String, Object> map = new HashMap<String, Object>();

		ArrayList<HashMap<String, String>> result = driver.getEscenario(modelo);
		ArrayList<HashMap<String, String>> result2 = driver.getPeriodo(modelo);
		ArrayList<HashMap<String, String>> result3 = driver.getCECONew(modelo);

		List<Encuesta> encuestas = Encuesta.find("modelo = ? and tipo = ?",
				modelo, tipo).fetch();

		Iterator<Encuesta> it = encuestas.iterator();
		Map<String, Object>[] arraymap = (Map<String, Object>[]) new Map[encuestas
				.size()];

		int j = 0;
		while (it.hasNext()) {
			Encuesta row = it.next();

			Map<String, Object> map1 = new HashMap<String, Object>();
			map1.put("optionValue", row.id);
			map1.put("optionDisplay", row.nombre);

			arraymap[j] = map1;
			j++;
		}

		map.put("escenario", Utilities.getArrayMap(result, "Nombre", "ID"));
		map.put("periodo", Utilities.getArrayMap(result2, "Nombre", "ID"));
		map.put("ceco", Utilities.getArrayMap(result3, "ID", "Nombre"));
		map.put("encuestas", arraymap);

		renderJSON(map);
	}

	/**
	 * Dependiendo del CECO, otiene el jefe correspondiente. Uso en CRUD de
	 * PermisoEncuesta
	 */
	public static void showUserData(String ceco) {

		User usr = User.find("jefeCECO = ? and activo = true ", ceco).first();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("usuario", usr);
		renderJSON(map);
	}




	//No usado a partir del 29-01-2016
	/**
	 * Resumen de Encuesta para Funcional
	 */
	/*public static void showEncuestaSummaryEstadistico(int idPermiso, String dimFuente) {

		Logger.info("El usuario " + Security.connected()
				+ " accedió a la vista de mostrar resumen de estadistico");

		int preguntaPos = 0;

		String[] keys = new String[] { "FuenteDimMemberRef1",
				"FuenteDimMemberRef2", "FuenteDimMemberRef3",
				"FuenteDimMemberRef4", "FuenteDimMemberRef5",
				"FuenteDimMemberRef6", "FuenteDimMemberRef7",
				"FuenteDimMemberRef8", "FuenteDimMemberRef9",
				"FuenteDimMemberRef10", "FuenteDimMemberRef11",
				"FuenteDimMemberRef12" };

		String[] keysDestino = new String[] { "DestinoDimMemberRef1",
				"DestinoDimMemberRef2", "DestinoDimMemberRef3",
				"DestinoDimMemberRef4", "DestinoDimMemberRef5",
				"DestinoDimMemberRef6", "DestinoDimMemberRef7",
				"DestinoDimMemberRef8", "DestinoDimMemberRef9",
				"DestinoDimMemberRef10", "DestinoDimMemberRef11",
				"DestinoDimMemberRef12" };

		ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		ArrayList<String> suma = new ArrayList<String>();
		ArrayList<HashMap<String, String>> estadistico = null;
		
		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);
		//Encuesta encuesta = perm.encuestaValidador;
		List<EncuestaForm> encuestas = encuesta.encuestas;

		String periodo = perm.periodo;
		String ceco = perm.ceco;
		String modelo = perm.modelo;
		
		Driver driver = new Driver();

		try {
		
			List<EncuestaForm> preguntas = encuesta.encuestas;
			Iterator<EncuestaForm> ite = preguntas.iterator();

			// Lineas de estadistico a ser mostrados en el resumen
			estadistico = driver.getEstadisticoRowDataSingle(modelo, dimFuente, preguntas.get(0).nombreConductor );
			
			StringBuilder sbb = new StringBuilder();

			for (String s : keys) {

				String dimension = estadistico.get(0).get(s);

				if (!dimension.equals("")) {
					sbb.append(dimension);
					sbb.append("-");
				}
			}
			
			sbb.deleteCharAt(sbb.length() - 1);
			estadistico.get(0).put("Nombre_Actividad", sbb.toString());

			while (ite.hasNext()) {

				EncuestaForm tmp = ite.next();
				ArrayList<HashMap<String, String>> result = 
					            driver.getEstadisticoRowDataSingle(modelo, dimFuente, tmp.nombreConductor);

				suma.add(driver.getFilasSumEstadistico(modelo, periodo, dimFuente, tmp.nombreConductor));

				Iterator<HashMap<String, String>> rowList = result.iterator();

				while (rowList.hasNext()) {

					HashMap<String, String> tmpRow = rowList.next();
					StringBuilder sb = new StringBuilder();

					for (String s : keysDestino) {

						String dimension = tmpRow.get(s);

						if (!dimension.equals("")) {
							sb.append(dimension);
							sb.append("-");
						}
					}
					sb.deleteCharAt(sb.length() - 1);
					tmpRow.put("Nombre_Actividad", sb.toString());
					tmpRow.put("pregunta", String.valueOf(preguntaPos));
				}

				preguntaPos++;
				items.addAll(result);
			}

			// Unir lineas iguales
			list = Utilities.getMergedList(items, preguntas.size());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		render(estadistico, perm, encuestas, items, list, suma, dimFuente);
	}
	*/




	/**
	 * Resumen de Encuesta para Funcional
	 *
	 * Actualizado al 22-02-2016
	 */
	public static void showEncuestaSummaryEstadistico(String dimFuente) {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de mostrar resumen de estadistico");

		/*int i = 0;
		float dqfTotal = 0;
		
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<HashMap<String, Object>> rows = new ArrayList<HashMap<String, Object>>();
		Map<String, Object> userdata = new HashMap<String, Object>();
		*/

		//Logger.info("Dimension fuente "+dimFuente);

		Driver driv = new Driver();

		String data[] = dimFuente.split("-");

        for (int i = 0; i < data.length; i++) {
			Logger.info("Valor data "+i+": "+data[i]);
		}

		//Obtener información de los parámetros del estadistico
		String segmento = data[0];
		String region = data[1];
		String unidad = data[2];
		String proceso = data[3];

		//Traer el estadistico a llenar
		HashMap<String, String> estActual = driv.getEstadisticoActual(region,proceso,unidad,segmento,0);

		//Logger.info(estActual.toString());
		String periodo = estActual.get("periodo");
		String modelo = estActual.get("modelo");

				
		// Traer descripciones para cada campo
		HashMap<String, String> desc_seg = driv.getDescMiembDim(segmento, periodo, modelo);
		HashMap<String, String> desc_reg = driv.getDescMiembDim(region, periodo, modelo);
		HashMap<String, String> desc_uni = driv.getDescMiembDim(unidad, periodo, modelo);
		HashMap<String, String> desc_pro = driv.getDescMiembDim(proceso, periodo, modelo);
			
		//Concatenar descripciones en un solo string
		StringBuilder dimension = new StringBuilder();
		dimension.append((desc_seg.get("Nombre")).toString());
		dimension.append("-");
		dimension.append((desc_reg.get("Nombre")).toString());
		dimension.append("-");
		dimension.append((desc_uni.get("Nombre")).toString());
		dimension.append("-");
		dimension.append((desc_pro.get("Nombre")).toString());
			
		dimension.deleteCharAt(dimension.length() - 1);

		String nombreActividad = dimension.toString();

		ArrayList<HashMap<String, String>> list = driv.getEstadisticoRowDataSingles(dimFuente);

		HashMap<String, String> dqfval = driv.getDQFSumEstadistico(dimFuente);

		String dqfSum = (dqfval.get("Total")).toString();

		/*try {

			ArrayList<HashMap<String, String>> filas = driv.getEstadisticoRowDataSingles(dimFuente);

			Iterator<HashMap<String, String>> it = result.iterator();
			
			while (it.hasNext()) {

				HashMap<String, String> tmp = it.next();
				HashMap<String, Object> row = new HashMap<String, Object>();

				row.put("id", tmp.get("DimensionDestino"));
				row.put("cell", tmp);
				rows.add(row);
				i++;
			}

			HashMap<String, String> dqfSum = 
				            driv.getDQFSumEstadistico(dimFuente);

			if (!dqfSum.get("Total").equals("")) {
				dqfTotal = Float.parseFloat(dqfSum.get("Total")); 
			}

			userdata.put("cell.DestinoDimMemberRef2", "Total:");
			userdata.put("cell.DQF", dqfTotal);

			map.put("total", 1);
			map.put("pages", 1);
			map.put("records", result.size());

			map.put("rows", rows);
			map.put("userdata", userdata);

			
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (ite.hasNext()) {

				EncuestaForm tmp = ite.next();
				ArrayList<HashMap<String, String>> result = 
					            driver.getEstadisticoRowDataSingle(modelo, dimFuente, tmp.nombreConductor);

				suma.add(driver.getFilasSumEstadistico(modelo, periodo, dimFuente, tmp.nombreConductor));

				Iterator<HashMap<String, String>> rowList = result.iterator();

				while (rowList.hasNext()) {

					HashMap<String, String> tmpRow = rowList.next();
					StringBuilder sb = new StringBuilder();

					for (String s : keysDestino) {

						String dimension = tmpRow.get(s);

						if (!dimension.equals("")) {
							sb.append(dimension);
							sb.append("-");
						}
					}
					sb.deleteCharAt(sb.length() - 1);
					tmpRow.put("Nombre_Actividad", sb.toString());
					tmpRow.put("pregunta", String.valueOf(preguntaPos));
				}

				preguntaPos++;
				items.addAll(result);
			}

		// Unir lineas iguales
		list = Utilities.getMergedList(items, preguntas.size());
		*/	

		render(nombreActividad, periodo, dimFuente, list, dqfSum);
	}
	



	/**
	 * Resumen de Estadístico
	 *
	 * Implementado al 04-03-2016
	 */
	public static void viewEncuestaSummaryEstadistico(String segmento, String region, String unidad, String proceso) {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de ver resumen de estadistico");

		Driver driv = new Driver();

		//Traer el estadistico a llenar
		HashMap<String, String> estActual = driv.getEstadisticoActual(region,proceso,unidad,segmento,1);

		//Logger.info(estActual.toString());
		String periodo = estActual.get("periodo");
		String modelo = estActual.get("modelo");

				
		// Traer descripciones para cada campo
		HashMap<String, String> desc_seg = driv.getDescMiembDim(segmento, periodo, modelo);
		HashMap<String, String> desc_reg = driv.getDescMiembDim(region, periodo, modelo);
		HashMap<String, String> desc_uni = driv.getDescMiembDim(unidad, periodo, modelo);
		HashMap<String, String> desc_pro = driv.getDescMiembDim(proceso, periodo, modelo);
			
		//Concatenar descripciones en un solo string
		StringBuilder dimension = new StringBuilder();
		dimension.append((desc_seg.get("Nombre")).toString());
		dimension.append("-");
		dimension.append((desc_reg.get("Nombre")).toString());
		dimension.append("-");
		dimension.append((desc_uni.get("Nombre")).toString());
		dimension.append("-");
		dimension.append((desc_pro.get("Nombre")).toString());
			
		dimension.deleteCharAt(dimension.length() - 1);

		String nombreActividad = dimension.toString();

		//Armar la DimensionFuente
		StringBuilder sb = new StringBuilder();
		sb.append(segmento);
		sb.append("-");
		sb.append(region);
		sb.append("-");
		sb.append(unidad);
		sb.append("-");
		sb.append(proceso);

		String dimFuente = sb.toString();

		Logger.info("Dimension fuente "+dimFuente);

		ArrayList<HashMap<String, String>> list = driv.getEstadisticoRowDataSingles(dimFuente);

		HashMap<String, String> dqfval = driv.getDQFSumEstadistico(dimFuente);

		String dqfSum = (dqfval.get("Total")).toString();

		render(nombreActividad, periodo, dimFuente, list, dqfSum);
	}



	/**
	 * Resumen Encuesta para funcional
	 */
	public static void viewEncuestaSummaryFuncional(int idPermiso) {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de mostrar resumen de cuestionario");

		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);
		Encuesta encuesta = perm.encuesta;
		List<EncuestaForm> encuestas = encuesta.encuestas;

		ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

		ArrayList<String> suma = new ArrayList<String>();
		
		String periodo = perm.periodo;
		String ceco = perm.ceco;
		String modelo = perm.modelo;
		
		try {
			Driver driver = new Driver();

			List<EncuestaForm> preguntas = encuesta.encuestas;
			Iterator<EncuestaForm> ite = preguntas.iterator();

			String[] keys = new String[] { "DestinoDimMemberRef1",
					"DestinoDimMemberRef2", "DestinoDimMemberRef3",
					"DestinoDimMemberRef4", "DestinoDimMemberRef5",
					"DestinoDimMemberRef6", "DestinoDimMemberRef7",
					"DestinoDimMemberRef8", "DestinoDimMemberRef9",
					"DestinoDimMemberRef10", "DestinoDimMemberRef11",
					"DestinoDimMemberRef12" };

			int preguntaPos = 0;

			while (ite.hasNext()) {

				EncuestaForm tmp = ite.next();
				
				String conductor = tmp.nombreConductor;

				ArrayList<HashMap<String, String>> result = driver.getFilasEncuesta(modelo, periodo, ceco, conductor, false);
				suma.add(driver.getFilasSum(modelo, periodo, ceco, conductor));
				
				Iterator<HashMap<String, String>> rowList = result.iterator();

				while (rowList.hasNext()) {

					HashMap<String, String> tmpRow = rowList.next();

					StringBuilder sb = new StringBuilder();

					for (String s : keys) {

						String dimension = tmpRow.get(s);

						if (!dimension.equals("")) {
							sb.append(dimension);
							sb.append("-");
						}
					}
					sb.deleteCharAt(sb.length() - 1);
					tmpRow.put("Nombre_Actividad", sb.toString());
					tmpRow.put("pregunta", String.valueOf(preguntaPos));
				}
				preguntaPos++;
				items.addAll(result);
			}
			// Unir lineas iguales
			list = Utilities.getMergedList(items, preguntas.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		render(perm, encuestas, items, list, suma);
	}

	/**
	 * Enviar Encuesta a Funcional
	 */
	public static void enviarFuncional(int idPermiso) {

		// Enviado a Funcional
		Status enviFuncional = Status.findById((long) 2);
		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);

		perm.status = enviFuncional;
		perm.save();

		flash.success("El cuestionario fue enviado al usuario Funcional");
		Encuestado.encuestado();
	}

	/**
	 * Cambio de Status de Encuesta
	 */
	public static void cambiarStatusFuncional(int idPermiso) {

		String save = params.get("_save");
		String reject = params.get("_reject");
		String msj = params.get("observaciones");
		String est = params.get("_est");

		if (save != null) {

			// Certificado
			Status certificado = Status.findById((long) 3);
			PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);

			Encuesta encuesta = perm.encuesta;
			List<EncuestaForm> encuestas = encuesta.encuestas;

			ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
			ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

			ArrayList<String> suma = new ArrayList<String>();
		
			String periodo = perm.periodo;
			String ceco = perm.ceco;
			String modelo = perm.modelo;
		
			try {
				Driver driver = new Driver();

				List<EncuestaForm> preguntas = encuesta.encuestas;
				Iterator<EncuestaForm> ite = preguntas.iterator();

				String[] keys = new String[] { "DestinoDimMemberRef1",
					"DestinoDimMemberRef2", "DestinoDimMemberRef3",
					"DestinoDimMemberRef4", "DestinoDimMemberRef5",
					"DestinoDimMemberRef6", "DestinoDimMemberRef7",
					"DestinoDimMemberRef8", "DestinoDimMemberRef9",
					"DestinoDimMemberRef10", "DestinoDimMemberRef11",
					"DestinoDimMemberRef12" };

				int preguntaPos = 0;

				while (ite.hasNext()) {

					EncuestaForm tmp = ite.next();
				
					String conductor = tmp.nombreConductor;

					ArrayList<HashMap<String, String>> result = driver.getFilasEncuesta(modelo, periodo, ceco, conductor, false);
					suma.add(driver.getFilasSum(modelo, periodo, ceco, conductor));
				
					Iterator<HashMap<String, String>> rowList = result.iterator();

					while (rowList.hasNext()) {

						HashMap<String, String> tmpRow = rowList.next();

						String part[] = tmpRow.get("DimensionDestino").split("-");
						String segmento = part[0];
						String region = part[1];
						String unidad = part[2];
						String proceso = part[3];

						String sql = null;
						boolean success = false;
					
						/*
						Se busca la cantidad de estadísticos existentes, si no existe se inserta
						y si existe no se inserta
						*/
						int cant = driver.getCantEstadistico(segmento,region,unidad,proceso);	

						if (cant<1){

					 	 Logger.info("Insertando estadistico");

					 	 sql = "INSERT INTO SASWeb.estadistico_com"
								+ " (segmento, region, unidad, proceso, funcional_rol_id,"
								+ " modelo, periodo, estatus) VALUES ('"+ segmento
								+ "','"+ region +"','"+ unidad +"','"+ proceso +"',3,"
								+ "'Modelo_COM','" + periodo + "',0)";
					 	 Logger.info(sql);
					 	 success = driver._queryWithoutResult(sql);

					 	 if (!success)
							break;
						}	 
					}
					preguntaPos++;
					items.addAll(result);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		

			perm.status = certificado;
			perm.save();
			flash.success("El cuestionario fue certificado!");
			
		} else if (reject != null) {

			// Rechazado
			Status rechazado = Status.findById((long) 4);
			PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);

			perm.status = rechazado;

			perm.observaciones = msj;
			perm.save();
			flash.success("El cuestionario fue rechazado y enviado al usuario correspondiente");
		}

		if (est != null)
			estadisticos();
		else
			funcional();
		funcional();
	}



	/**
	 * Cambio de Status de Estadístico
	 * Usado a partir del 04-03-2016
	 *  
	 */
	public static void cambiarStatusEstadistico(String dimFuente) {

		String save = params.get("_save");
		Driver driv = new Driver();

		String data[] = dimFuente.split("-");

        for (int i = 0; i < data.length; i++) {
			Logger.info("Valor data "+i+": "+data[i]);
		}

		//Obtener información de los parámetros del estadistico
		String segmento = data[0];
		String region = data[1];
		String unidad = data[2];
		String proceso = data[3];

		if (save != null) {
			
			try {
				
				boolean result = driv.certificarEstadistico(region,proceso,unidad,segmento);
				
				if (!result) {
					flash.success("Hubo un error al certificar el estadístico");
					estadisticos();
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			flash.success("¡Muchas gracias!, ¡El estadístico fue certificado!");
		} 
		
		estadisticos();
		
	}


	public static void cargaInicial() {
		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de carga inicial");
		Driver driv = new Driver();
		ArrayList<HashMap<String, String>> fechas = driv.getPeriodoTable();
		render(fechas);
	}

	public static void pruebaEst1() {
		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de prueba de estadisticos");
		render();
	}

	public static void cargaInicialETS(String periodo, String escenario) {
		Logger.info("Corriendo job de carga inicial");
		Logger.info("Escenario: "+escenario+" - Periodo: "+periodo);
		String filename = "C:\\Documents and Settings\\Administrador\\Escritorio\\sasweb\\sasweb\\transformaciones\\carga_inicial\\JOB_CAR_INI.kjb";
		// Puesta en ejecucion del job por medio de un hilo

		try
		{
			Jobs jobCargaInicial = new Jobs(filename, periodo, escenario);
			jobCargaInicial.runDimensionRelacionJob();
		} catch (Exception e) {
			System.out.println("Error en: " + e);
		}

	}

	public static void showCargaInicialLog() throws IOException {
	

		String sep = System.getProperty("file.separator");

		String filename = "C:\\Documents and Settings\\Administrador\\Escritorio\\sasweb\\sasweb\\transformaciones\\carga_inicial\\logs\\carga_inicial.log";

		FileInputStream in = new FileInputStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine = null, tmp;

		while ((tmp = br.readLine()) != null) {
			strLine = tmp;
		}

		String lastLine = strLine;
		;
		renderHtml(lastLine);
	}

}