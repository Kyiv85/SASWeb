package controllers;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import models.PermisoEncuesta;
import models.Rol;
import models.User;
import notifiers.Mails;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;
import db.Driver;

/**
 * Asignación de Cuestionarios
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
@Check("generador")
@With(Secure.class)
@CRUD.For(PermisoEncuesta.class)
public class PermisoEncuestas extends CRUD {

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

			ArrayList<User> usuarios = getUsuarios();
			//ArrayList<HashMap<String, String>> revisores = driv.getRevisores();

			//render(type, object, usuarios, revisores);
			render(type, object, usuarios);
		} catch (TemplateNotFoundException e) {
			render("CRUD/blank.html", type, object);
		}
	}

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

			ArrayList<User> usuarios = getUsuarios();
			//ArrayList<HashMap<String, String>> revisores = driv.getRevisores();

			//render(type, object, usuarios, revisores);
			render(type, object, usuarios);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object);
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

		Driver driv = new Driver();

		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {

				ArrayList<User> usuarios = getUsuarios();
				//ArrayList<HashMap<String, String>> revisores = driv.getRevisores();
				
				render(request.controller.replace(".", "/") + "/blank.html",
						type, object, usuarios);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type, object);
			}
		}
		object._save();

		PermisoEncuesta permiso = (PermisoEncuesta) object;
		HashMap<String, String> cecoNombre = driv.getCECONombre(permiso.ceco,
				permiso.modelo);

		/*Mails.notificacionRegistrador(permiso.usuario, permiso.revisor,
				permiso.ceco + "-" + cecoNombre.get("Nombre"), permiso.periodo);
		Mails.notificacionValidador(permiso.revisor, permiso.usuario,
				permiso.ceco + "-" + cecoNombre.get("Nombre"), permiso.periodo);
		System.out.println("Sending emails");*/

		flash.success(Messages.get("crud.created", type.modelName));
		if (params.get("_save") != null) {
			redirect(request.controller + ".list");
		}
		if (params.get("_saveAndAddAnother") != null) {
			redirect(request.controller + ".blank");
		}
		redirect(request.controller + ".show", object._key());
	}

	/**
	 * 
	 * Crear nuevo objeto
	 * 
	 * @param id
	 */
	public static void save(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Model object = type.findById(id);
		notFoundIfNull(object);
		Binder.bind(object, "object", params.all());
		validation.valid(object);

		Driver driv = new Driver();

		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {
					
					ArrayList<User> usuarios = getUsuarios();
					//ArrayList<HashMap<String, String>> revisores = driv.getRevisores();

					render(request.controller.replace(".", "/") + "/show.html",
						type, object, usuarios);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		}
		object._save();


		PermisoEncuesta permiso = (PermisoEncuesta) object;
		HashMap<String, String> cecoNombre = driv.getCECONombre(permiso.ceco,
				permiso.modelo);

		// Notificar por correo
		/*
		Mails.notificacionRegistrador(permiso.usuario, permiso.revisor,
				permiso.ceco + "-" + cecoNombre.get("Nombre"), permiso.periodo);
		Mails.notificacionValidador(permiso.revisor, permiso.usuario,
				permiso.ceco + "-" + cecoNombre.get("Nombre"), permiso.periodo);
		System.out.println("Sending emails");*/

		flash.success(Messages.get("crud.saved", type.modelName));
		if (params.get("_save") != null) {
			redirect(request.controller + ".list");
		}
		redirect(request.controller + ".show", object._key());
	}

	
	//27-01-2016 Traer TODOS los usuarios por orden alfabético
	//Revisión por CECO eliminada
	static ArrayList<User> getUsuarios() {

		//Encuestado
		Rol rol = Rol.findById((long) 2);
		
		//List<User> todos = User.all().fetch();
		List<User> todos = User.find("ORDER BY nombre ASC").fetch();

		ArrayList<User> usuarios = new ArrayList<User>();
		Iterator<User> it = todos.iterator();

		while (it.hasNext()) {

			User tmp = it.next();
			if (tmp.rol.contains(rol)) {
				usuarios.add(tmp);
			}
		}

		return usuarios;
	}


}
