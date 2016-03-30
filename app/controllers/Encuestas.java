package controllers;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.Encuesta;
import models.PermisoEncuesta;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;
import db.Driver;

@Check("generador")
@With(Secure.class)
@CRUD.For(Encuesta.class)
public class Encuestas extends CRUD {

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

	public static void create() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		Model object = (Model) constructor.newInstance();
		Binder.bind(object, "object", params.all());
		validation.valid(object);
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {

				Driver driv = new Driver();
				ArrayList<HashMap<String, String>> modelos = driv.getModeloNew();

				render(request.controller.replace(".", "/") + "/blank.html",
						type, object, modelos);

			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type, object);
			}
		}
		object._save();
		flash.success(Messages.get("crud.created", type.modelName));
		if (params.get("_save") != null) {
			redirect(request.controller + ".list");
		}
		if (params.get("_saveAndAddAnother") != null) {
			redirect(request.controller + ".blank");
		}
		redirect(request.controller + ".show", object._key());
	}

	public static void delete(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Model object = type.findById(id);
		notFoundIfNull(object);

		boolean noDelete = false;
		Encuesta encu = (Encuesta) object;
		// Ver si ha sido asignado
		List<PermisoEncuesta> asignado = PermisoEncuesta.find(
				"encuesta = ?", encu).fetch();

		// Si ha sido asignado o tiene preguntas
		if (asignado.size() != 0) {
			flash.error("No se puede borrar. Cuestionario está asignado");
			noDelete = true;
		} else if (encu.encuestas.size() != 0) {
			flash.error("No se puede borrar. Cuestionario tiene preguntas asignadas");
			noDelete = true;
		}

		if (noDelete) {
			redirect(request.controller + ".show", object._key());
		}

		try {
			object._delete();
		} catch (Exception e) {
			flash.error(Messages.get("crud.delete.error", type.modelName));
			redirect(request.controller + ".show", object._key());
		}
		flash.success(Messages.get("crud.deleted", type.modelName));
		redirect(request.controller + ".list");
	}

}
