package models;

import javax.persistence.Entity;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
public class Valor extends Model {

	@MaxSize(100)
	public String nombre;

	public String toString() {
		return nombre;
	}

}
