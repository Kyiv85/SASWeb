package controllers;

import models.User;
import play.mvc.With;

/**
 * Controlador de Seguridad de SASWeb
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
@Check("seguridad")
@With(Secure.class)
@CRUD.For(User.class)
public class Seguridad extends CRUD {

}