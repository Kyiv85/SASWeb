package models;

import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.Logger;

import utils.Unique;

import db.Driver;

import controllers.CRUD.Hidden;

@Entity
public class estadistico_com extends Model {

	@Required
	@Unique()
	@MaxSize(200)
	public String segmento;

	@Required
	@Unique()
	@MaxSize(200)
	public String region;

	@Required
	@Unique()
	@MaxSize(200)
	public String unidad;

	@Required
	@Unique()
	@MaxSize(200)
	public String proceso;

	@OneToOne
	@Required
	public Rol funcional_rol;

	@MaxSize(255)
	public String modelo;

	@MaxSize(255)
	public String periodo;

	public boolean estatus;

	public estadistico_com(String segmento, String region, String unidad, String proceso, String modelo, String periodo, boolean estatus){
		this.segmento = segmento;
		this.region = region;
		this.unidad = unidad;
		this.proceso = proceso;
		this.modelo = modelo;
		this.periodo = periodo;
		this.estatus = estatus;
	}
}