package controllers;

import db.Driver;
import models.*;
import notifiers.Mails;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.i18n.Messages;
import play.mvc.Controller;

import java.io.*;
import java.util.*;


/**
 * Created by gedica on 19/03/2015.
 */
public class Masivo extends Controller {

    public static void index() {
        render("masivo/masivo.html");
       // return redirect(routes.masivo.index);
    }

    public static void masivo() {
        render();
        // return redirect(routes.masivo.index);
    }

    public static void cargaCsv(File arch) {
        // StringBuffer cad = new StringBuffer("");
//        FileInputStream is = null;
        StringTokenizer cad2;
        PermisoEncuesta permiso ;
        final String SEPARADOR = ";";

        try {
            BufferedReader buff = new BufferedReader(new FileReader(arch));
            String line ;

//            is = new FileInputStream(arch);
//            while ((ch = is.read()) != -1) {
//                cad.append((char) ch);
            int linea = 0;
            while ((line = buff.readLine()) != null)  {

                cad2 = new StringTokenizer( line,SEPARADOR );
                Logger.info("Linea:  " + line);

                if (linea > 0) {

                    permiso = new PermisoEncuesta();
                    while (cad2.hasMoreTokens()) {

                        permiso.encuesta = Encuesta.find("byNombre", cad2.nextToken()).first();
                        Logger.info("Encuesta:  " + permiso.encuesta.nombre);
            // System.out.println("Encuesta:" + permiso.encuesta.toString());
                        //permiso.encuestaValidador = Encuesta.find("byNombre",cad2.nextToken()).first();
                        //Logger.info("EncuestaValidador:  " + cad2.toString());
                        permiso.modelo = cad2.nextToken();
                        Logger.info("Modelo:  " + cad2.toString());
                        permiso.periodo = cad2.nextToken();
                        Logger.info("Periodo:  " + cad2.toString());
                        permiso.escenario = cad2.nextToken();
                        permiso.ceco = cad2.nextToken(); //verificar si CONVERTIR
                        //permiso.revisor = User.find("byUsuario",cad2.nextToken()).first();
                        permiso.usuario = User.find("byUsuario",cad2.nextToken()).first();
                        permiso.status = Status.find("byDescripcion", cad2.nextToken()).first();
                        cad2.nextToken();
                        permiso.__Activo = true ; //ESTA CABLEADO
                        // Enviar correo
                        /*
                        Mails.notificacionRegistrador(permiso.usuario, permiso.revisor,
                                permiso.ceco, permiso.periodo);
                        Mails.notificacionValidador(permiso.revisor, permiso.usuario,
                                permiso.ceco, permiso.periodo);
                        */
                        permiso.save();
                    }

                    if (params.get("_save") != null) {
                        //aqui va mensaje
                        //redirect(request.controller + ".masivo");
                    }
                }
                linea++;
            }
//            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

  //      renderText(cad);
    }


}
