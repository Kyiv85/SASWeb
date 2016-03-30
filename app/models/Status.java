package models;

import javax.persistence.Entity;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
public class Status extends Model {

	@MaxSize(255)
	public String descripcion;

	public Status(String desc) {
		this.descripcion = desc;
	}

	public String toString() {
		return descripcion;
	}

}
