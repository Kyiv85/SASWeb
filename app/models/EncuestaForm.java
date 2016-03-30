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

@Entity
public class EncuestaForm extends Model {

	@Required
	@MaxSize(100)
	public String nombre;
	@Required
	@ManyToOne
	public Encuesta encuesta;
	@Required
	@OneToOne
	public Valor tipoValor;
	@Required
	@MaxSize(100)
	public String unidadMedida;

	@Required
	@CheckWith(UniquePositionCheck.class)
	public int posicion;

	@Required
	@MaxSize(200)
	public String nombreConductor;

	public String toString() {
		return nombre;
	}

	static class UniquePositionCheck extends Check {

		@Override
		public boolean isSatisfied(Object validatedObject, Object value) {

			EncuestaForm encuestafrm = (EncuestaForm) validatedObject;

			List<EncuestaForm> tmp = EncuestaForm.find("byPosicion", value)
					.fetch();

			Iterator<EncuestaForm> it = tmp.iterator();

			if (encuestafrm.encuesta == null)
				return false;

			Integer val = (Integer) value;

			if (val.intValue() == 0) {
				setMessage("encuestaform.valorinvalido");
				return false;
			}

			if (tmp.size() == 0)
				return true;
			else {

				while (it.hasNext()) {

					EncuestaForm plant = it.next();

					// Si pertenecen a la misma encuesta
					if (plant.encuesta.nombre
							.equals(encuestafrm.encuesta.nombre)) {

						if (!plant.id.equals(encuestafrm.id)) {
							setMessage("encuestaform.errorencuesta");
							return false;
						}
					}
				}
			}

			return true;
		}
	}

}
