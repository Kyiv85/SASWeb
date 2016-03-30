package notifiers;

import models.User;
import play.mvc.Mailer;

/**
 * Notificación via e-mail
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class Mails extends Mailer {

	/**
	 * Notificación de Usuario Validador
	 * 
	 */
	public static void notificacionValidador(User revisor, User usuario,
			String ceco, String periodo) {
		setSubject("Notificacion SASWeb %s", revisor.nombre);
		addRecipient(revisor.email);
		setFrom("Sistema SASWeb <prueba@0269.com.ve>");
		send(revisor, usuario, ceco, periodo);
	}

	/**
	 * Notificación de Usuario Funcional
	 * 
	 */
	public static void notificacionRegistrador(User usuario, User revisor,
			String ceco, String periodo) {
		setSubject("Notificacion SASWeb %s", usuario.nombre);
		addRecipient(usuario.email);
		setFrom("Sistema SASWeb <prueba@0269.com.ve>");
		send(usuario, revisor, ceco, periodo);
	}

}