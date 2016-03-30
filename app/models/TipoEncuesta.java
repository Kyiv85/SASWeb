package models;

import javax.persistence.Entity;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
public class TipoEncuesta extends Model {

	@MaxSize(255)
	public String descripcion;

	public TipoEncuesta(String desc) {
		this.descripcion = desc;
	}

	public String toString() {
		return descripcion;
	}

}
