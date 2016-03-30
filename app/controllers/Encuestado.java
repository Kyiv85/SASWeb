package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import models.Encuesta;
import models.EncuestaForm;
import models.PermisoEncuesta;
import models.Status;
import models.TipoEncuesta;
import models.User;
import models.estadistico_com;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import utils.Utilities;
import db.Driver;

/**
 * Controlador modulo Encuestado de SASWeb
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
@With(Secure.class)
public class Encuestado extends Controller {

	/**
	 * Vista de Encuestado
	 */
	@Check("encuestado")
	public static void encuestado() {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de Encuestado");

		String usr = Security.connected();
		User user = User.find("byUsuario", usr).<User> first();

		Status noEnv = Status.findById((long) 1);
		// Enviado a funcional
		Status envi = Status.findById((long) 2);
		Status validado = Status.findById((long) 3);
		Status rechazado = Status.findById((long) 4);

		TipoEncuesta tipo = TipoEncuesta.findById((long) 2);

		List<PermisoEncuesta> noactualizados = PermisoEncuesta
				.find("(status = ? or status = ?) and usuario = ? and __Activo = true and encuesta.tipo = ? ",
						noEnv, rechazado, user, tipo).fetch();

		List<PermisoEncuesta> actualizados = PermisoEncuesta
				.find("(status = ? or status = ?) and usuario = ? and __Activo = true and encuesta.tipo = ? ",
						envi, validado, user, tipo).fetch();

		render(noactualizados, actualizados);
	}

	/**
	 * Vista de Llenado de Encuesta
	 */
	@Check("encuestado")
	public static void llenarEncuesta(int idPermiso) {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de llenar Encuesta");

		Driver driv = new Driver();

		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);
		Encuesta encuesta = perm.encuesta;

		ArrayList<HashMap<String, String>> actividades = driv.getActividades(encuesta.modelo, perm.periodo, perm.escenario);
		HashMap<String, List> selectBoxes = new HashMap<String, List>();
		ArrayList<HashMap<String, String>> usu = driv.getUsuarioId(Security.connected());
		int usu_id = Integer.parseInt(usu.get(0).get("id"));
		int encu_id = (encuesta.id).intValue();

		Logger.info("Id del usuario "+usu_id);
		ArrayList<HashMap<String, String>> preg_encu = driv.getPreguntasFiltradasEncuesta(encuesta.modelo, usu_id, encu_id);

		for (int i = 0; i < actividades.size(); i++) {

			String dim = (actividades.get(i)).get("_ID");
			selectBoxes.put(dim, driv.getActividadesData(dim, encuesta.modelo, perm.periodo, perm.escenario));
		}

		render(perm, encuesta, actividades, selectBoxes, preg_encu);
	}

	/**
	 * Resumen de la encuesta
	 */
	public static void showEncuestaSummary(int idPermiso, String ceco, String periodo) {

		Logger.info("El usuario " + Security.connected()
				+ " accedio a la vista de mostrar resumen de cuestionario");

		PermisoEncuesta perm = PermisoEncuesta.findById((long) idPermiso);
		Encuesta encuesta = perm.encuesta;
		List<EncuestaForm> encuestas = encuesta.encuestas;

		ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		
		ArrayList<String> suma = new ArrayList<String>();
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

				ArrayList<HashMap<String, String>> result = 
					                   driver.getFilasEncuesta(modelo, periodo, ceco, conductor, false);

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

                //Se verifica si están todas las filiales y grupos en AsignacionTemporal y si no se insertan
                

		render(perm, encuestas, items, list, suma);
	}

	/**
	 * Resumen de la Encuesta
	 */
	public static void viewEncuestaSummary(int idPermiso) {

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

}
