package controllers;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.EncuestaForm;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.mvc.With;
import db.Driver;

import play.Logger;

/**
 * Controlador Principal para Cuestionarios
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
@Check("generador")
@With(Secure.class)
@CRUD.For(EncuestaForm.class)
public class EncuestaForms extends CRUD {

	/**
	 * 
	 * Vista de objeto
	 * 
	 * @param id
	 */
	public static void show(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Model object = type.findById(id);
		notFoundIfNull(object);
		try {
			Driver driv = new Driver();
			ArrayList<HashMap<String, String>> modelos = driv.getModeloNew();

			render(type, object, modelos);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object);
		}
	}

	/**
	 * 
	 * Vista de nuevo objeto
	 * 
	 * @param id
	 */
	public static void blank() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		Model object = (Model) constructor.newInstance();
		try {
			Driver driv = new Driver();
			ArrayList<HashMap<String, String>> modelos = driv.getModeloNew();

			render(type, object, modelos);
		} catch (TemplateNotFoundException e) {
			render("CRUD/blank.html", type, object);
		}
	}

	//Eliminar pregunta de encuesta personalizado 05-02-2016
	/*public static void delete(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Model object = type.findById(id);
		notFoundIfNull(object);

		//Obtener información de la pregunta y ver cuántas hay
		EncuestaForm preg = EncuestaForm.findById(new Long(id));
		List<EncuestaForm> preguntas = EncuestaForm.find("encuesta_id = ?",
				preg.encuesta).fetch();

		int cant = preguntas.size();
		Logger.info("Cantidad de preguntas "+cant);

		try {
			object._delete();
		} catch (Exception e) {
			flash.error(Messages.get("crud.delete.error", type.modelName));
			redirect(request.controller + ".show", object._key());
		}

		//Se trae la nueva cantidad de preguntas
		preguntas = EncuestaForm.find("encuesta_id = ?",
				preg.encuesta).fetch();
		cant = preguntas.size();

		Driver driv = new Driver();
		//Se actualizan las posiciones de la encuesta
		for (int i = 0; i < cant; i++) {
		 		
			sql = "UPDATE SASWeb.dbo.EncuestaForm
				SET posicion = 4
					WHERE encuesta_id = 102
					and id = 215";
			Logger.info(sql) ;
			success = driv._queryWithoutResult(sql);

			if (!success)
				break;
		}

		flash.success(Messages.get("crud.deleted", type.modelName));
		redirect(request.controller + ".list");
	}*/

}
