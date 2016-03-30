package models;

import java.util.HashMap;

/*
import javax.persistence.Entity;
import javax.persistence.OneToOne;
*/
import javax.persistence.*;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import db.Driver;

import controllers.CRUD.Hidden;

@Entity
public class PermisoEncuesta extends Model {

	public boolean __Activo = true;

	@OneToOne
	@Required
	@org.hibernate.annotations.BatchSize(size = 500)
	public User usuario;

	@OneToOne
	@Required
	public Encuesta encuesta;
	
	@OneToOne
	@Required
	public Rol funcional_rol;

	@Required
	public String modelo;
	@Required
	@MaxSize(100)
	public String ceco;
	@Required
	@MaxSize(50)
	public String periodo;
	@Required
	@MaxSize(50)
	public String escenario;
	
	@OneToOne
	@Required
	public Status status;

	@MaxSize(255)
	public String observaciones;

	public String toString() {
		return usuario + " " + modelo;
	}

	public String getCECONombre() {	

		try{
			Driver driver = new Driver();

			HashMap<String, String> tmp = driver.getCECONombre(this.ceco,
					this.modelo);
			return tmp.get("Nombre");
		} catch (Exception e) {
			return "Ceco no definido";
		}
	}

	public void setFuncionalRol() {
		Rol func_rol = Rol.findById((long) 3);
        this.funcional_rol = func_rol;
    }

}
