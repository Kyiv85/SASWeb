package models;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "configuracion_general")
public class Configuracion extends Model {

	public boolean __AdminActivo;
	public boolean __EncuestadoActivo;
	public boolean __FuncionalActivo;
	public boolean __GeneradorActivo;

	public String toString() {
		return "Configuración";
	}

}
