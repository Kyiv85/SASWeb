package utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import db.Driver;

/**
 * @author gerardo
 * 
 */
public class Utilities {

	public static ArrayList<HashMap<String, Object>> getMergedList(
			ArrayList<HashMap<String, String>> items, int columns) {

		boolean wasUpdated = false;
		Iterator<HashMap<String, String>> ite = items.iterator();

		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

		while (ite.hasNext()) {

			HashMap<String, String> tmpRow = ite.next();

			Iterator<HashMap<String, Object>> listIte = list.iterator();

			while (listIte.hasNext()) {

				HashMap<String, Object> tmpItem = listIte.next();
				// Si hay un elemento en la nueva lista con
				// Nombre_Actividad igual en lista pasada por parametros
				if (tmpItem.get("Nombre_Actividad").equals(
						tmpRow.get("Nombre_Actividad"))) {

					ArrayList<String> curArr = (ArrayList<String>) tmpItem
							.get("DQF");
					int preguntaCount = Integer.valueOf(tmpRow.get("pregunta"));
					curArr.set(preguntaCount, tmpRow.get("DQF"));
					wasUpdated = true;
					System.out.println(curArr);

				}
			}

			// si el item no fue encontrado, lo agregamos
			if (!wasUpdated) {

				ArrayList<String> dqfArr = new ArrayList<String>(columns);

				// Ingresar blancos
				int preguntaCount = Integer.valueOf(tmpRow.get("pregunta"));

				for (int i = 0; i < columns; i++) {
					if (i == preguntaCount) {
						dqfArr.add(tmpRow.get("DQF")); // Agrego el primer
														// DQF(si existen mas)
					} else {
						dqfArr.add("");
					}
				}

				System.out.println(dqfArr);

				HashMap<String, Object> newItem = new HashMap<String, Object>();

				newItem.put("Nombre_Actividad", tmpRow.get("Nombre_Actividad"));
				newItem.put("_idpregunta", tmpRow.get("_idpregunta"));
				newItem.put("DQF", dqfArr);

				list.add(newItem);

			}
			wasUpdated = false;
		}
		return list;
	}

	public static ArrayList<HashMap<String, String>> getMergedListEstadistico(
			ArrayList<HashMap<String, String>> items) {

		boolean wasAdded = false;
		Iterator<HashMap<String, String>> ite = items.iterator();

		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		while (ite.hasNext()) {

			HashMap<String, String> tmpRow = ite.next();

			Iterator<HashMap<String, String>> listIte = list.iterator();

			while (listIte.hasNext()) {

				HashMap<String, String> tmpItem = listIte.next();

				if (tmpItem.get("Nombre_Actividad").equals(
						tmpRow.get("Nombre_Actividad"))) {
					wasAdded = true;
				}
			}
			// si el item no fue encontrado, lo agregamos
			if (!wasAdded) {
				list.add(tmpRow);
			}
			wasAdded = false;
		}
		return list;
	}

	public static Map<String, Object>[] getArrayMap(
			ArrayList<HashMap<String, String>> result, String field,
			String fieldID) {

		Iterator<HashMap<String, String>> it = result.iterator();
		Map<String, Object>[] arraymap = (Map<String, Object>[]) new Map[result
				.size()];

		int j = 0;
		while (it.hasNext()) {
			HashMap<String, String> row = it.next();

			Map<String, Object> map1 = new HashMap<String, Object>();
			map1.put("optionValue", row.get(field));
			map1.put("optionDisplay", row.get(fieldID));

			arraymap[j] = map1;
			j++;
		}

		return arraymap;
	}

	public static String getTimeStamp() {

		// cambiar formato dependiendo del server
		String DATE_FORMAT = "yyyy-dd-MM";

		//String DATE_FORMAT = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		Calendar c1 = Calendar.getInstance(); // today

		return sdf.format(c1.getTime());
	}

}
