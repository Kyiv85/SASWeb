package models;

import javax.persistence.Entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Rol extends Model {

	@MaxSize(255)
	public String descripcion;

	public Rol(String desc) {
		this.descripcion = desc;
	}

	public String toString() {
		return descripcion;
	}

}
