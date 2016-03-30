package controllers;

import models.Configuracion;
import play.mvc.With;

/**
 * Controlador de Configuración para SASWeb
 * 
 * Solo de uso para la interfaz de Admin
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
@Check("generador")
@With(Secure.class)
@CRUD.For(Configuracion.class)
public class Configuraciones extends CRUD {

}
