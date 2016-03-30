package models;

import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.data.validation.Required;
import play.db.jpa.Model;
import utils.Unique;

@Entity
public class Encuesta extends Model {

	@Required
	@Unique()
	public String nombre;
	@OneToOne
	@Required
	public TipoEncuesta tipo;
	@Required
	public String modelo;

	@OneToMany(mappedBy = "encuesta")
	public List<EncuestaForm> encuestas;

	/*public void Encuesta() {
    	this.tipo = TipoEncuesta.findById((long) 2);
	}*/
	public String toString() {

		return nombre;
	}
}
