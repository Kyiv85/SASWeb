package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import play.Logger;
import play.db.DB;

/**
 * Clase manejadora de base de datos
 *
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 *
 */
public class Driver {

	private static Connection _conn;

	/**
	 * Constructor por defecto
	 *
	 */
	public Driver() {

		try {

			// Usamos la conección provista por Play!
			Connection conn = DB.getConnection();
			this._conn = conn;

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Utilidad de consulta a Base de datos
	 *
	 */
	public boolean _queryWithoutResult(String sql) {
		try

		{
			Statement stmt;

			// ResultSet rs;
			stmt = this._conn.createStatement();
			// Send the SQL query to the database
			stmt.executeUpdate(sql);
			stmt.close();
			return true;
		}

		catch (Exception ex) {
			Logger.debug("queryWithoutResult: ");
			ex.printStackTrace();
			Logger.debug(" from [" + sql + "]");
			Logger.debug("Error occurred when running _queryWithoutResult: "
					+ ex.getMessage() + " from [" + sql + "]");

			return false;
		}
	}

	/**
	 * Utilidad de consulta a Base de datos
	 *
	 */
	public boolean _transactionWithoutResult(String... sqls) {

		try {

			Logger.info("AutoCommit" + this._conn.getAutoCommit());

			// this._conn.setAutoCommit(false);

			Statement stmt;
			stmt = this._conn.createStatement();

			for (int i = 0; i < sqls.length; i++) {
				// Send the SQL query to the database
				if (!sqls[i].equalsIgnoreCase(""))
					stmt.executeUpdate(sqls[i]);
			}

			this._conn.commit();
			stmt.close();

		} catch (Exception ex) {
			Logger.debug("transactionWithoutResult: ");
			ex.printStackTrace();

			for (int i = 0; i < sqls.length; i++) {
				Logger.info(" from [" + sqls[i] + "]");
			}
			Logger.info("Error occurred when running _queryWithoutResult: "
					+ ex.getMessage());

			try {
				this._conn.rollback();
				// this._conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		}

		return true;
	}

	/**
	 * Utilidad de consulta a Base de datos
	 *
	 */
	public static ArrayList<HashMap<String, String>> _queryWithManyResults(String sql) {
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();

		try {

			Statement stmt;
			ResultSet rs;
			ResultSetMetaData rsmd;
			stmt = _conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			// Send the SQL query to the database
			rs = stmt.executeQuery(sql);
			rsmd = rs.getMetaData();
			int count = rsmd.getColumnCount();

			// If there's a row we found...
			while (rs.next()) {
				HashMap<String, String> map = new HashMap<String, String>();

				for (int i = 1; i <= count; i++) {
					String value = rs.getString(i);
					if (value == null) {
						value = "";
					} else if (value.equals("null")) {
						value = "";
					}

					map.put(rsmd.getColumnName(i), value);
				}
				results.add(map);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.debug("Error occurred when running queryWithManyResults: "
					+ e.getMessage() + " from [" + sql + "]");
			return null;
		}
		return results;
	}

	/**
	 * Utilidad de consulta a Base de datos
	 *
	 */
	

	/*public static HashMap<String, String> _queryWithResult(String sql) {

		HashMap<String, String> map = new HashMap<String, String>();

		try {

			Statement stmt;
			ResultSet rs;
			ResultSetMetaData rsmd;
			stmt = _conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			// Send the SQL query to the database
			rs = stmt.executeQuery(sql);

			// If there's a row we found...
			if (rs.first()) {
				rsmd = rs.getMetaData();
				int count = rsmd.getColumnCount();

				for (int i = 1; i <= count; i++) {
					String value = rs.getString(i);
					if (value == null) {
						value = "";
					} else if (value.equals("null")) {
						value = "";
					}

					map.put(rsmd.getColumnName(i), value);
				}

				//rs.close();
				//stmt.close();
				return map;
			}
		} catch (Exception e) {
			Logger.info("Error occurred when running queryWithResult: "
					+ e.getMessage() + " from [" + sql + "]");
			return null;
		} finally {
			rs.close();
		    stmt.close();
		}

		return null;
	}*/

	public static HashMap<String, String> _queryWithResult(String sql) {

        HashMap<String, String> map = new HashMap<String, String>();
        Connection conn = DB.getConnection();
        ResultSet rs = null;
        ResultSetMetaData rsmd;
        try {
                Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
                try {
                        rs = stmt.executeQuery(sql);
                        if (rs.first()) {
                            rsmd = rs.getMetaData();
                            int count = rsmd.getColumnCount();

                            for (int i = 1; i <= count; i++) {
                                String value = rs.getString(i);
                                if (value == null) {
                                    value = "";
                                } else if (value.equals("null")) {
                                    value = "";
                                }

                                map.put(rsmd.getColumnName(i), value);
                            }
                        }
                } finally {
                        //rs.close();
                        //stmt.close();
                }
            } catch (Exception e) {
                Logger.info("Error occurred when running queryWithResult: "
                            + e.getMessage() + " from [" + sql + "]");
                return null;
            } finally {                
                //conn.close();
                return map;
            }
    }

    /*public static HashMap<String, String> _queryWithResult(String sql) {
    	HashMap<String, String> map = new HashMap<String, String>();
        Connection conn = DB.getConnection();
        ResultSet rs = null;
        ResultSetMetaData rsmd;
        try {
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        	try {
        		rs = stmt.executeQuery(sql);
                conn.close();
                if (rs.first()) {
                    rsmd = rs.getMetaData();
                    int count = rsmd.getColumnCount();

                    for (int i = 1; i <= count; i++) {
                        String value = rs.getString(i);
                        if (value == null) {
                            value = "";
                        } else if (value.equals("null")) {
                            value = "";
                        }

                        map.put(rsmd.getColumnName(i), value);
                    }
                }
        	}catch (SQLException es) {
        		Logger.info("ERROR EN EL TRY INTERNO "+es);	
        	}

        	//rs.close();        	
        	
        	Logger.info("Paso ");
        }catch (Exception ex){
        	Logger.info("ERROR EN EL PRIMER TRY "+ex);
        }finally{
            return map;
        }
        

	}*/

	/**
	 * Obtener modelos existentes
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getModelo() {
		Logger.debug("Obteniendo Modelos");

		String sql = "select Nombre " + "from TMP_CS.modelo "
				+ "where __activo = 1 " + " UNION "
				+ "select Nombre " + "from TMP_COM.modelo "
				+ "where __activo = 1 " + " UNION " + "select Nombre "
				+ "from TMP_RED.modelo " + "where __activo = 1 ";

		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);
		return result;
	}

	/**
	 * Obtener modelos existentes al 25-01-2016
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getModeloNew() {
		Logger.debug("Obteniendo Modelos");

		String sql = "select Nombre " + "from TMP_CS.modelo "
				+ "where __activo = 1 " + " UNION "
				+ "select Nombre " + "from TMP_COM.modelo "
				+ "where __activo = 1 " + " UNION " + "select Nombre "
				+ "from TMP_RED.modelo " + "where __activo = 1 ";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);
		return result;
	}


	/**
	 * Obtener Escenarios existentes
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getEscenario(String modelo) {
		Logger.info("Obteniendo Escenario");

		String sql = "select distinct ID as Nombre, ID from TMP_CS.escenario where _NombreModelo ='"
				+ modelo
				+ "'"
				+ " union "
				+ " select distinct ID as Nombre, ID from TMP_COM.escenario where _NombreModelo ='"
				+ modelo
				+ "'"
				+ " union "
				+ " select distinct ID as Nombre, ID from TMP_RED.escenario where _NombreModelo ='"
				+ modelo
				+ "'";

		Logger.info(sql);

		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	/**
	 * Obtener Periodos
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getPeriodo(String modelo) {
		Logger.info("Obteniendo Escenario");

		String sql = "select distinct Nombre, Nombre as ID from TMP_CS.periodo where _NombreModelo ='"
				+ modelo
				+ "'"
				+ " union "
				+ " select distinct Nombre, Nombre as ID from TMP_COM.periodo where _NombreModelo ='"
				+ modelo
				+ "'"
				+ " union "
				+ " select distinct Nombre, Nombre as ID from TMP_RED.periodo where _NombreModelo ='"
				+ modelo
				+ "'";

		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	/**
	 * Obtener CECO existentes
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getCECO(String modelo) {
		Logger.info("Obteniendo Escenario");

		String sql = " select Nombre, ID from ("
				+ " select distinct ID, ID + '-' + Nombre as Nombre from TMP_CS.miembrodimension where _NombreModelo ='"
				+ modelo
				+ "'"
				+ " and _IDDImension = 'CR'"
				+ " union "
				+ " select distinct ID, ID + '-' + Nombre as Nombre from TMP_COM.miembrodimension where _NombreModelo ='"
				+ modelo
				+ "'"
				+ " and _IDDImension = 'CR'"
				+ " union "
				+ " select distinct ID, ID + '-' + Nombre as Nombre from TMP_RED.miembrodimension where _NombreModelo ='"
				+ modelo
				+ "'"
				+ " and _IDDImension = 'CR'"
				+ " ) baseDatos order by ID";

		Logger.debug(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}


	/**
	 * Obtener CECO existentes con gasto y no asignados al 27-01-2016
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getCECONew(String modelo) {
		Logger.info("Obteniendo cecos con gasto y no asignados");

		String bd = getModelNameAsDBNew(modelo);

		String sql = "SELECT DISTINCT ID, CONCAT( ID, '-', Nombre ) AS Nombre FROM "+ bd 
					+ ".centrosresponsabilidad cg, " + bd + ".gastos g"
  					+ " WHERE g.IDModelo = '" + modelo + "'"
    				+ " AND g._IDCentrosResponsabilidad = cg.ID"
    				+ " AND cg.id NOT IN (SELECT ceco "
                    + " FROM SASWeb.PermisoEncuesta"
                    + " WHERE modelo = '" + modelo + "'"
                    + " AND CAST(ceco AS char(255) CHARACTER SET utf8) ="
                    + " CAST(cg.ID AS char(255) CHARACTER SET utf8) )";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}





	/**
	 * Obtener CECONombre
	 *
	 * @return
	 */

	public HashMap<String, String> getCECONombre(String IDCeco, String modelo) {
		Logger.info("Obteniendo CecoNombre");

		String sql = " select distinct Nombre from TMP_CS.miembrodimension where _NombreModelo = '"
				+ modelo
				+ "' AND ID ='"
				+ IDCeco
				+ "'"
				+ " and _IDDImension = 'CR'"
				+ " union "
				+ " select distinct Nombre from TMP_COM.miembrodimension where _NombreModelo = '"
				+ modelo
				+ "' AND ID ='"
				+ IDCeco
				+ "'"
				+ " and _IDDImension = 'CR'"
				+ " union "
				+ " select distinct Nombre from TMP_RED.miembrodimension where _NombreModelo = '"
				+ modelo
				+ "' AND ID ='"
				+ IDCeco
				+ "'"
				+ " and _IDDImension = 'CR'";
		Logger.info(sql);
		HashMap<String, String> result = this._queryWithResult(sql);
		return result;
	}

	/**
	 * Obtener Filiales
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getFilial(String modelo) {
		Logger.info("Obteniendo Filial");

		String bd = getModelNameAsDBNew(modelo);			
		int maxNivel = getMaxNivelDimension(bd, "FI");		

		String sql = "select ID, Nombre from TMP_CS.miembrodimension where _IDDimension = 'FI' and nivel = "+ maxNivel +" "
				+ " union "
				+ "select ID, Nombre from TMP_RED.miembrodimension where _IDDimension = 'FI' and nivel = "+ maxNivel +" "
				+ " union "
				+ "select ID, Nombre from TMP_COM.miembrodimension where _IDDimension = 'FI' and nivel = "+ maxNivel +" ";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this._queryWithManyResults(sql);
		return result;
	}



	/**
	 * Obtener Filiales cambio al 02-03-2016
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getFilialNew(String ceco, String modelo) {
		Logger.info("Obteniendo Filial");

		String bd = getModelNameAsDBNew(modelo);			
		int maxNivel = getMaxNivelDimension(bd, "FI");

		String sql = "SELECT DISTINCT f.ID"
 					+ " FROM " + bd + ".gastos g," + bd + ".filial f"
					+ " WHERE g._IDCentrosResponsabilidad = '" + ceco +"'" 
					+ " AND g._IDFilial = IDNum";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this._queryWithManyResults(sql);
		return result;

	}




	public static int getMaxNivelDimension(String bd, String dimension) {

		String maxSQLDimAnterior = "select MAX(nivel) as MaxNivel" + " from "
				+ bd + ".miembrodimension" + " WHERE _IDDimension = '"
				+ dimension + "'";

		Logger.info(maxSQLDimAnterior);
		HashMap<String, String> dim = _queryWithResult(maxSQLDimAnterior);

		return Integer.parseInt(dim.get("MaxNivel"));
	}

	/**
	 * Obtener DimensionNombre
	 *
	 * @return
	 */
	private HashMap<String, String> getDimensionNombre(String ID) {
		//Logger.info("Obteniendo Dimension Nombre");

		String sql = "select  Nombre from TMP_CS.miembrodimension where ID  = '"
				+ ID
				+ "'  "
				+ " union "
				+ "select  Nombre from TMP_RED.miembrodimension where ID = '"
				+ ID
				+ "'  "
				+ " union "
				+ "select  Nombre from TMP_COM.miembrodimension where ID = '"
				+ ID + "' ";

		Logger.debug(sql);

		HashMap<String, String> result = this._queryWithResult(sql);
		return result;
	}

	public ArrayList<HashMap<String, String>> getFilasEncuesta(String periodo, String ceco, boolean conductor) {
		
		return null;
	}

	/**
	 * Obtener Filas de las encuestas
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> 
	                 getFilasEncuesta(String modelo, String periodo, String ceco, String conductor, boolean activo) {
		
		Logger.info("getFilasEncuesta() ");
			
		String bd = getModelNameAsDB(modelo);

		String destinos = "DestinoDimMemberRef1, DestinoDimMemberRef2,DestinoDimMemberRef3,DestinoDimMemberRef4,DestinoDimMemberRef5,"
				+ "DestinoDimMemberRef6,DestinoDimMemberRef7,DestinoDimMemberRef8,DestinoDimMemberRef9, DestinoDimMemberRef10, "
				+ "DestinoDimMemberRef11, DestinoDimMemberRef12 ";

		StringBuffer sql = new StringBuffer(); 
			
			
			sql.append( "select distinct DQF, " +
			   destinos +
			   " ,DimensionDestino from "+ bd +".AsignacionTemporal where " +
			   " _IDPeriodo = '"+ periodo+"' " +
			   " and _TipoModuloABCFuente = 'RESOURCE'" +
			   " and _NombreConductor = '"+ conductor + "' " +
			   " and _TipoModuloABCDestino = 'ACTIVITY'"+
			   " and FuenteDimMemberRef2 = '"+ ceco +"'" +
			   " and FuenteDimMemberRef3 in " +
			   "                 (select distinct GrupoCuenta from TMP_CS.EquivalenciaGC where " +
			   "                  _IDconductor = '"+ conductor +"') ") ;
			if(activo)
				sql.append(" and __ACTIVO = 1");
			else
				sql.append(" and __ACTIVO = 0");
			   		
		Logger.info(sql.toString());
		ArrayList<HashMap<String, String>> result = 
			                             this._queryWithManyResults(sql.toString());

		Iterator<HashMap<String, String>> it = result.iterator();

		String[] keys = new String[] { "DestinoDimMemberRef1",
				"DestinoDimMemberRef2", "DestinoDimMemberRef3",
				"DestinoDimMemberRef4", "DestinoDimMemberRef5",
				"DestinoDimMemberRef6", "DestinoDimMemberRef7",
				"DestinoDimMemberRef8", "DestinoDimMemberRef9",
				"DestinoDimMemberRef10", "DestinoDimMemberRef11",
				"DestinoDimMemberRef12" };
		// Human-readable name
		while (it.hasNext()) {

			HashMap<String, String> tmp = it.next();

			for (int i = 0; i < keys.length; i++) {
				String value = tmp.get(keys[i]);
				if (!value.equals(""))
					tmp.put(keys[i], getDimensionNombre(value).get("Nombre"));
			}

		}

		return result;
	}

	

	/**
	 * Obtener Filas de las encuestas
	 *
	 * @return
	 */
	public String getFilasSum(String modelo, String periodo, String ceco, String conductor) {
		
		Logger.info("getFilasSum()  ");
		
		String bd = Driver.getModelNameAsDB(modelo);

		String sql =  "select SUM(dqf) as suma from( select distinct DQF,"+
			   " DimensionDestino from "+ bd +".AsignacionTemporal where " +
			   " _IDPeriodo = '"+ periodo+"' " +
			   " and _TipoModuloABCFuente = 'RESOURCE'" +
			   " and _NombreConductor = '"+ conductor + "' " +
			   " and _TipoModuloABCDestino = 'ACTIVITY'" +
			   " and FuenteDimMemberRef2 = '"+ ceco +"'" +
			   " and FuenteDimMemberRef3 in " +
			   "                 (select distinct GrupoCuenta from TMP_CS.EquivalenciaGC where " +
			   "                  _IDconductor = '"+ conductor +"')) as tabla2" ;
			   		
		
		Logger.info(sql);
		HashMap<String, String> result = 
			                             this._queryWithResult(sql);

		return result.get("suma");
	}


	/**
	 * Obtener Filas de las encuestas ESTADÍSTICO
	 *
	 * @return
	 */
	public String getFilasSumEstadistico(String modelo, String periodo, String dimFuente, String conductor) {
		
		Logger.info("getFilasSumEstadistico()  ");
		
		String bd = Driver.getModelNameAsDB(modelo);

		String sql =  "select SUM(dqf) as suma from( select distinct DQF,"+
			   " DimensionDestino from "+ bd +".AsignacionTemporal where " +
			   " _IDPeriodo = '"+ periodo+"' " +
			   " and _TipoModuloABCFuente = 'ACTIVITY'" +
			   " and _NombreConductor = '"+ conductor + "' " +
			   " and _TipoModuloABCDestino = 'COSTOBJECT'" +
			   " and DimensionFuente = '"+ dimFuente +"'" +
			   ") as tabla2" ;
			   		
		Logger.info(sql);
		HashMap<String, String> result = 
			                             this._queryWithResult(sql);

		return result.get("suma");
	}


	/**
	 * getEncuestaRowDataDim
	 * 
	 * @param idPermiso
	 * @param idPregunta
	 * @param fuente
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getEncuestaRowDataDim(
			String idPermiso, String idPregunta, String fuente) {
		Logger.info("getEncuestaRowDataDim()");

		String destinos = "DestinoDimMemberRef1, DestinoDimMemberRef2,DestinoDimMemberRef3,DestinoDimMemberRef4,DestinoDimMemberRef5,"
				+ "DestinoDimMemberRef6,DestinoDimMemberRef7,DestinoDimMemberRef8,DestinoDimMemberRef9, DestinoDimMemberRef10, "
				+ "DestinoDimMemberRef11, DestinoDimMemberRef12 ";

		String sql = "select distinct  DQF, _idpregunta , _idInsert, "
				+ destinos
				+ ", DimensionFuente  from TMP_CS.AsignacionTemporal where _idPermisoEncuesta  = '"
				+ idPermiso
				+ "'  "
				+ "  AND _idpregunta ='"
				+ idPregunta
				+ "' AND DimensionFuente='"
				+ fuente
				+ "' "
				+ " union "
				+ "select distinct DQF, _idpregunta , _idInsert,"
				+ destinos
				+ " , DimensionFuente from TMP_RED.AsignacionTemporal where _idPermisoEncuesta = '"
				+ idPermiso
				+ "'  "
				+ "  AND _idpregunta ='"
				+ idPregunta
				+ "' AND DimensionFuente='"
				+ fuente
				+ "' "
				+ " union "
				+ "select distinct DQF, _idpregunta , _idInsert, "
				+ destinos
				+ " , DimensionFuente from TMP_COM.AsignacionTemporal where _idPermisoEncuesta = '"
				+ idPermiso
				+ "'  "
				+ "  AND _idpregunta ='"
				+ idPregunta
				+ "' AND DimensionFuente='"
				+ fuente
				+ "' ";

		Logger.debug(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		Iterator<HashMap<String, String>> it = result.iterator();

		String[] keys = new String[] { "DestinoDimMemberRef1",
				"DestinoDimMemberRef2", "DestinoDimMemberRef3",
				"DestinoDimMemberRef4", "DestinoDimMemberRef5",
				"DestinoDimMemberRef6", "DestinoDimMemberRef7",
				"DestinoDimMemberRef8", "DestinoDimMemberRef9",
				"DestinoDimMemberRef10", "DestinoDimMemberRef11",
				"DestinoDimMemberRef12" };
		// Human-readable name
		while (it.hasNext()) {

			HashMap<String, String> tmp = it.next();

			for (int i = 0; i < keys.length; i++) {

				String value = tmp.get(keys[i]);
				if (!value.equals(""))
					tmp.put(keys[i], getDimensionNombre(value).get("Nombre"));
			}

		}

		return result;
	}

	public ArrayList<HashMap<String, String>> getEncuestaRowDataSingle(String modelo, String dimensionFuente) {
		
		Logger.info("getEncuestaRowDataSingle()");

		String bd = getModelNameAsDB(modelo);
		
		String destinos = "DestinoDimMemberRef1, DestinoDimMemberRef2,DestinoDimMemberRef3,DestinoDimMemberRef4,DestinoDimMemberRef5,"
				+ "DestinoDimMemberRef6,DestinoDimMemberRef7,DestinoDimMemberRef8,DestinoDimMemberRef9, DestinoDimMemberRef10, "
				+ "DestinoDimMemberRef11, DestinoDimMemberRef12 ";
		
		String[] keys = new String[] { "DestinoDimMemberRef1",
				"DestinoDimMemberRef2", "DestinoDimMemberRef3",
				"DestinoDimMemberRef4", "DestinoDimMemberRef5",
				"DestinoDimMemberRef6", "DestinoDimMemberRef7",
				"DestinoDimMemberRef8", "DestinoDimMemberRef9",
				"DestinoDimMemberRef10", "DestinoDimMemberRef11",
				"DestinoDimMemberRef12" };


		String[] dimensions = dimensionFuente.split("-");
		StringBuffer strbuf = new StringBuffer();
		
		for (int i = 0; i < dimensions.length; i++)
			strbuf.append("FuenteDimMemberRef"+(i+1)+"='"+ dimensions[i] + "' AND ");
			
		String sql = "select distinct  DQF, "
				+ destinos
				+ ", DimensionDestino  from "+bd+".AsignacionTemporal WHERE " +
				  strbuf.toString() + " 1 = 1 ";
		
		Logger.info(sql);
		
		ArrayList<HashMap<String, String>> result = this._queryWithManyResults(sql);
		Iterator<HashMap<String, String>> it = result.iterator();

		// Human-readable name
		while (it.hasNext()) {

			HashMap<String, String> tmp = it.next();

			for (int i = 0; i < keys.length; i++) {

				String value = tmp.get(keys[i]);
				if (!value.equals(""))
					tmp.put(keys[i], getDimensionNombre(value).get("Nombre"));
			}

		}

		return result;
	}


	public ArrayList<HashMap<String, String>> getEstadisticoRowDataSingle(String modelo, String dimensionFuente, String conductor) {
		
		Logger.info("Entrando en getEstadisticoRowDataSingle()");

		String bd = getModelNameAsDBNew(modelo);
		
		String destinos = "DestinoDimMemberRef1, DestinoDimMemberRef2,DestinoDimMemberRef3,DestinoDimMemberRef4,DestinoDimMemberRef5,"
				+ "DestinoDimMemberRef6,DestinoDimMemberRef7,DestinoDimMemberRef8,DestinoDimMemberRef9, DestinoDimMemberRef10, "
				+ "DestinoDimMemberRef11, DestinoDimMemberRef12 ";

		String fuentes = "FuenteDimMemberRef1, FuenteDimMemberRef2,FuenteDimMemberRef3,FuenteDimMemberRef4,FuenteDimMemberRef5,"
			+ "FuenteDimMemberRef6,FuenteDimMemberRef7,FuenteDimMemberRef8,FuenteDimMemberRef9, FuenteDimMemberRef10, "
			+ "FuenteDimMemberRef11, FuenteDimMemberRef12 ";

		String[] keys = new String[] { "DestinoDimMemberRef1",
				"DestinoDimMemberRef2", "DestinoDimMemberRef3",
				"DestinoDimMemberRef4", "DestinoDimMemberRef5",
				"DestinoDimMemberRef6", "DestinoDimMemberRef7",
				"DestinoDimMemberRef8", "DestinoDimMemberRef9",
				"DestinoDimMemberRef10", "DestinoDimMemberRef11",
				"DestinoDimMemberRef12" };

		String[] keysFuente = new String[] { "FuenteDimMemberRef1",
				"FuenteDimMemberRef2", "FuenteDimMemberRef3",
				"FuenteDimMemberRef4", "FuenteDimMemberRef5",
				"FuenteDimMemberRef6", "FuenteDimMemberRef7",
				"FuenteDimMemberRef8", "FuenteDimMemberRef9",
				"FuenteDimMemberRef10", "FuenteDimMemberRef11",
				"FuenteDimMemberRef12" };

		String[] dimensions = dimensionFuente.split("-");
		StringBuffer strbuf = new StringBuffer();
		
		for (int i = 0; i < dimensions.length; i++)
			strbuf.append("FuenteDimMemberRef"+(i+1)+"='"+ dimensions[i] + "' AND ");
			
		String sql = "select distinct  DQF, "
				+ destinos + ", " + fuentes  
				+ ", DimensionDestino  from "+bd+".AsignacionTemporal WHERE " +
				  strbuf.toString() + " _NombreConductor = '"+ conductor +"' ";
		
		Logger.info(sql);
		
		ArrayList<HashMap<String, String>> result = this._queryWithManyResults(sql);
		Iterator<HashMap<String, String>> it = result.iterator();

		// Human-readable name
		while (it.hasNext()) {

			HashMap<String, String> tmp = it.next();

			for (int i = 0; i < keys.length; i++) {

				String value = tmp.get(keys[i]);
				if (!value.equals(""))
					tmp.put(keys[i], getDimensionNombre(value).get("Nombre"));
			}
			
			for (int i = 0; i < keysFuente.length; i++) {

				String value = tmp.get(keysFuente[i]);
				if (!value.equals(""))
					tmp.put(keysFuente[i], getDimensionNombre(value).get("Nombre"));
			}


		}

		return result;
	}



	/**
	 * Trae datos cargados del stadistico
	 * Cambio al 04-03-2016
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getEstadisticoRowDataSingles(String dimensionFuente) {
		
		Logger.info("Entrando en getEstadisticoRowDataSingles()");
	
		String sql = "SELECT A.DQF,A.DestinoDimMemberRef1,A.DestinoDimMemberRef2,"
					+ "_NombreConductor FROM TMP_COM.AsignacionTemporal A,"
					+ "TMP_COM.miembrodimension TC,TMP_COM.miembrodimension PC"
					+ " WHERE DimensionFuente = '"+dimensionFuente+"'"
					+ " AND _TipoModuloABCFuente = 'ACTIVITY' "
					+ " AND _TipoModuloABCDestino = 'COSTOBJECT' AND TC._IDDimension = 'TC'"
					+ " AND PC._IDDimension = 'PC' AND TC.ID = DestinoDimMemberRef1"
					+ " AND PC.ID = DestinoDimMemberRef2";
		
		Logger.info(sql);
		
		ArrayList<HashMap<String, String>> result = this._queryWithManyResults(sql);
		
		return result;
	}

	/**
	 * Trae ID del miembrodimension
	 * 04-03-2016
	 *
	 * @return
	 */
	public String getIdMiembroDimension(String descripcion, String dimension) {

		Logger.info("Entrando en getIdmiembrodimension");
	
		String sql = "SELECT ID FROM TMP_COM.miembrodimension"
					+ " WHERE Nombre = '"+descripcion+"'"
					+ " AND _IDDimension = '"+dimension+"'";
		
		Logger.info(sql);
		
		HashMap<String, String> result = this._queryWithResult(sql);
		
		return (result.get("ID").toString());

	}

	
	/**
	 * Certifica encuesta
	 *
	 * @return
	 */
	public boolean certificarEncuestaRowData(String modelo, String dimFuente) {
		Logger.info("certificarEncuestaRowData");

		String bd = getModelNameAsDB(modelo);

		String sql = "UPDATE "+bd+".AsignacionTemporal SET __activo = 1 where DimensionDestino = '"+dimFuente+ "'; ";
		
		Logger.info(sql);
		return this._queryWithoutResult(sql);
        //return this._queryWithManyResults(sql); devuelve varios registros
        //return this._queryWithResult(sql); devuelve 1 registro
	}



	/**
	 * Certifica estadisticos
	 *
	 * @return
	 */
	public boolean certificarEstadistico(String region, String proceso, String unidad, String segmento) {
		Logger.info("certificarEstadistico");

		String sql = "UPDATE SASWeb.estadistico_com"
						+" SET estatus = 1"
						+" WHERE region = '"+region+"'" 
						+" AND proceso = '"+proceso+"'" 
						+" AND unidad = '"+unidad+"'" 
						+" AND segmento = '"+segmento+"'";
		
		Logger.info(sql);
		
		return this._queryWithoutResult(sql);

	}



	/**
	 * Obtener Conductores EDITADO 26-01-2016
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getConductor(String _NombreModelo) {

		//String bd = getModelNameAsDB(_NombreModelo);
		String bd = getModelNameAsDBNew(_NombreModelo);


		// get informacion de los conductores para este usuario
		String sql = "select Nombre, Nombre as ID from " + bd
				+ ".conductor";
		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	

	/**
	 * Obtener Conductores en encuesta existente que ya no 
	 * Estén asignados 26-01-2016
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getConductorEncuesta(int id, String _NombreModelo) {

		//String bd = getModelNameAsDB(_NombreModelo);
		String bd = getModelNameAsDBNew(_NombreModelo);


		// get informacion de los conductores para este usuario
		Logger.info("Obteniendo conductores para encuesta "+id);
		
		String sql = "SELECT c.Nombre, c.Nombre AS ID FROM " + bd
						+ ".conductor c WHERE c.Nombre NOT IN "
						+ "(SELECT nombreConductor "
                        + " FROM SASWeb.EncuestaForm"
                        + " WHERE encuesta_id = "+ id +")"
                        + " AND c.Nombre IN ("
						+ " SELECT DISTINCT _IDConductor"
						+ " FROM " + bd + ".EquivalenciaGC)";

		Logger.info(sql);

		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}



	/**
	 * Obtener Posición de la última pregunta de encuensta
	 *
	 * @return
	 */
	public static int getLastPosicion(Integer id) {

		Logger.info("Obteniendo la ultima posicion de la encuesta");

		String sql = "SELECT IFNULL(MAX(posicion),0) posicion" 
						+ " FROM SASWeb.EncuestaForm"
						+ " WHERE encuesta_id = "+id;
		Logger.info(sql);

		HashMap<String, String> res = _queryWithResult(sql);

		return Integer.parseInt(res.get("posicion"));
	}



	/**
	 * Obtener cantidad de estadisticos existentes con los parámetros dados
	 *
	 * @return
	 */
	public static int getCantEstadistico(String segmento, String region, String unidad, String proceso) {

		Logger.info("Obteniendo cantidad de estadisticos existentes");

		String sql = "SELECT COUNT(*) valor FROM SASWeb.estadistico_com"
				+ " WHERE segmento = '"+segmento+"' AND region = '"+region
				+ "' AND unidad = '"+unidad+"' AND proceso = '"+proceso+"'";
		Logger.info(sql);

		HashMap<String, String> res = _queryWithResult(sql);

		return Integer.parseInt(res.get("valor"));
	}



	/**
	 * Obtener descripcion de miembrodimension
	 *
	 * @return
	 */
	public static HashMap<String, String> getDescMiembDim(String idDim, String periodo, String modelo) {

		Logger.info("Obteniendo descripcion miembrodimension");

		String sql = "SELECT Nombre FROM TMP_COM.miembrodimension"
				+ " WHERE ID = '"+idDim+"'"
				+ " AND _IDPeriodo = '"+periodo+"'"
				+ " AND _NombreModelo = '"+modelo+"'";

		Logger.info(sql);

		HashMap<String, String> res = _queryWithResult(sql);

		return res;
	}



	/**
	 * Obtener Actividades
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getActividades(String modelo,
			String periodo, String escenario) {
		Logger.info("Obteniendo Actividades");

		String bd = getModelNameAsDB(modelo);

		String sql = "select d.nombre as nombre , od._ID as id " + "from " + bd
				+ ".ordendimension od, " + bd + ".dimension d "
				+ "where od._TipoModuloABC = 'ACTIVITY' "
				+ "and od._ID = d.ID " + "and od._IDPeriodo = '" + periodo
				+ "' " + "and od._IDEscenario = '" + escenario + "' "
				+ "and od._NombreModelo = '" + modelo + "' "
				+ "and od._IDPeriodo = d._IDPeriodo "
				+ "and od._IDEscenario = d._IDEscenario "
				+ "and od._NombreModelo = d._NombreModelo "
				+ "order by od.Jerarquia_Dimension ";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);
		return result;
	}


	/**
	 * Obtener Preguntas filtradas
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getPreguntasFiltradasEncuesta(String modelo, int usuario, int encuesta) {
		
		//Las preguntas ya vienen filtradas al momento de crear la encuesta
		//Query sin filtro aplicado al 01-02-2016

		Logger.info("Obteniendo preguntas de la encuesta");
		
		String sql =  "SELECT DISTINCT f.id, f.nombre, f.nombreConductor,"
					+ " f.posicion, f.unidadMedida, f.encuesta_id, f.tipoValor_id"
					+ " FROM SASWeb.EncuestaForm f," 
					+ " SASWeb.PermisoEncuesta p"
					+ " WHERE p.usuario_id = '" + usuario + "'"
					+ " AND f.encuesta_id = '"+ encuesta + "'"
					+ " AND p.encuesta_id = f.encuesta_id";			
		
		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);
		return result;
	}


	/**
	 * Obtener Objetos de Costo
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getObjetoCosto(String modelo,
			String periodo, String escenario) {
		Logger.info("Obteniendo Objetos de Costo");

		String bd = getModelNameAsDB(modelo);

		String sql = "select d.nombre as nombre , od._ID as id " + "from " + bd
				+ ".ordendimension od, " + bd + ".dimension d "
				+ "where od._TipoModuloABC = 'COSTOBJECT' "
				+ "and od._ID = d.ID " + "and od._IDPeriodo = '" + periodo
				+ "' " + "and od._IDEscenario = '" + escenario + "' "
				+ "and od._NombreModelo = '" + modelo + "' "
				+ "and od._IDPeriodo = d._IDPeriodo "
				+ "and od._IDEscenario = d._IDEscenario "
				+ "and od._NombreModelo = d._NombreModelo "
				+ "order by od.Jerarquia_Dimension ";

		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);
		return result;
	}

	/**
	 * Obtener Datos de actividades
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getActividadesData(String id,
			String modelo, String periodo, String escenario) {
		Logger.info("Obteniendo Actividades Data");


		String sql = "select Nombre as nombre, ID as id from TMP_CS.miembrodimension "
				+ "where _IDDimension = '"
				+ id
				+ "' "
				+ " and _nombreModelo = '"
				+ modelo
				+ "' "
				+
				// "order by __JerarquiaReferencia " +
				"union "
				+ "select Nombre as nombre, ID as id from TMP_RED.miembrodimension "
				+ "where _IDDimension = '"
				+ id
				+ "' "
				+ " and _nombreModelo = '"
				+ modelo
				+ "' "
				+
				// "order by __JerarquiaReferencia " +
				"union "
				+ "select Nombre as nombre, ID as id from TMP_COM.miembrodimension "
				+ "where _IDDimension = '"
				+ id
				+ "' "
				+ " and _nombreModelo = '"
				+ modelo
				+ "' ";
		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	/**
	 * Obtener Datos de actividades
	 *
	 * Ajustes al 03-03-2016
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getActividadesDataNEW(String id) {
		Logger.info("Obteniendo Actividades Data NEW");

		String sql = "SELECT A.ID, A.Nombre" 
					+ " FROM TMP_COM.miembrodimension A"
					+ " WHERE A._IDDimension = '"+id+"'"
  					+ " AND NOT EXISTS (SELECT B.Padre_ID"
                    + " FROM TMP_COM.miembrodimension B"
                    + " WHERE B._IDDimension = '"+id+"'"
                    + " AND A.ID = B.Padre_ID)";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	/**
	 * Obtener Datos de actividades con modelo
	 *
	 * Ajustes al 03-03-2016
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getActividadesDataNEW(String id, String modelo) {
		Logger.info("Obteniendo Actividades Data NEW");

		String bd = getModelNameAsDB(modelo);

		String sql = "SELECT A.ID, A.Nombre" 
					+ " FROM "+bd+".miembrodimension A"
					+ " WHERE A._IDDimension = '"+id+"'"
					+ " AND A.ID NOT IN ('SE_ASC_CO','SE_ND_CND'"
					+ ",'US_CND','US_ASC_CO','PR_ND_CND','RG_RE_CND',"
					+ "'PR_ASC_CO','TC_CND','TC_ASC','PC_ND_CND','PC_ASC_CO')"
  					+ " AND NOT EXISTS (SELECT B.Padre_ID"
                    + " FROM "+bd+".miembrodimension B"
                    + " WHERE B._IDDimension = '"+id+"'"
                    + " AND A.ID = B.Padre_ID)";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	/**
	 * Obtener Cuentas
	 *
	 * @return
	 */

	public ArrayList<HashMap<String, String>> getCuenta(String conductor,
			String nombreModelo) {
		Logger.info("Getting cuentas");

		String sql = "select GrupoCuenta from TMP_CS.EquivalenciaGC where _IDConductor = '"
				+ conductor
				+ "' and _NombreModelo = '"
				+ nombreModelo
				+ "' "
				+ " union "
				+ "select GrupoCuenta from TMP_RED.EquivalenciaGC where _IDConductor = '"
				+ conductor
				+ "' and _NombreModelo = '"
				+ nombreModelo
				+ "' "
				+ " union "
				+ "select GrupoCuenta from TMP_COM.EquivalenciaGC where _IDConductor = '"
				+ conductor
				+ "' and _NombreModelo = '"
				+ nombreModelo
				+ "' ";
		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}





	/**
	 * Obtener Cuentas solicitud al 04-02-2016
	 *
	 * @return
	 */

	public ArrayList<HashMap<String, String>> getCuentasNew(String conductor,
			String modelo, String ceco) {
		Logger.info("Getting cuentas NEW");

		String bd = getModelNameAsDB(modelo);

		String sql = "SELECT DISTINCT A.GrupoCuenta FROM "+bd+".gastos B,"
    				+ bd+".EquivalenciaGC A WHERE B.IDModelo = '"+modelo+"'"
    				+ " AND B._IDCentrosResponsabilidad = '"+ceco+"'"
  					+ " AND B._CuentaEquivalenciaGC = A.Cuenta "
  					+ " AND A._IDConductor = '"+conductor+"'"
  					+ " AND B.Total <> 0";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}





	/**
	 * Obtener cantidad de conductores de estadistico para dimFuente dada 
	 * solicitud al 04-03-2016
	 *
	 * @return
	 */

	public int getCantConductorEst(String dimFuente) {

		Logger.info("Obteniendo cantidad de conductor de estadistico");

		String sql = "SELECT COUNT(*) valor"
  					+ " FROM TMP_COM.asignacion"
					+ " WHERE IDFuente = '"+dimFuente+"'"
  					+ " AND _TipoModuloABCFuente = 'ACTIVITY'"
  					+ " AND _TipoModuloABCDestino = 'COSTOBJECT'";

		Logger.info(sql);
		HashMap<String, String> res = _queryWithResult(sql);

		return Integer.parseInt(res.get("valor"));
	}



	/**
	 * Verificar si existen datos para estadistico en AsignacionTemporal 
	 * solicitud al 04-03-2016
	 *
	 * @return
	 */
	public int getCantAsignacionesEst(String dimFuente) {

		Logger.info("Verificando si existen datos del estadistico en asignacion temporal");

		String sql = "SELECT COUNT(*) valor FROM TMP_COM.AsignacionTemporal"
  					+ " WHERE DimensionFuente = '"+dimFuente+"'"
  					+ " AND _TipoModuloABCFuente = 'ACTIVITY'"
  					+ " AND _TipoModuloABCDestino = 'COSTOBJECT'";

		Logger.info(sql);
		HashMap<String, String> res = _queryWithResult(sql);

		return Integer.parseInt(res.get("valor"));
	}



	/**
	 * Obtener Conductor para Estadístico 
	 * solicitud al 04-03-2016
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getConductorEst(String dimFuente) {

		//int cond = getCantAsignacionesEst(dimFuente);
		//int cant = getCantConductorEst(dimFuente);

		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

		Logger.info("Obteniendo conductor de estadistico");
		
		/*if(cond>0){

			String sql = "SELECT DISTINCT _NombreConductor AS id, _NombreConductor AS nombre" 
					+ " FROM TMP_COM.AsignacionTemporal"
					+ " WHERE DimensionFuente = '"+dimFuente+"'"
  					+ " AND _TipoModuloABCFuente = 'ACTIVITY'"
  					+ " AND _TipoModuloABCDestino = 'COSTOBJECT'"
  					+ " ORDER BY _NombreConductor ASC";

		 	Logger.info(sql);
		 	result = this._queryWithManyResults(sql);
		
		}
		else { 
			if(cant==0){  		
		*/ 
				String sql = "SELECT Nombre AS id, Nombre AS nombre"
  					+ " FROM TMP_COM.conductor"
  					+ " WHERE Nombre NOT IN ('% BP RSC x % Ventas Efectivas','% Compensacion Variable x Adiciones',"
  					+ " '% de Dedicación a los Servicios','Asignacion Directa','Contratados y Contratistas','Contrato de Mantenimiento o de Servicio',"
  					+ " 'Mano de Obra y Otros Gastos','Materiales y Consumibles','Porcentaje')";

		 		Logger.info(sql);
		 		result = this._queryWithManyResults(sql);
		
		/*	}else{
		
				String sql = "SELECT DISTINCT _NombreConductor AS id, _NombreConductor AS nombre"
  						+ " FROM TMP_COM.asignacion"
						+ " WHERE IDFuente = '"+dimFuente+"'"
  						+ " AND _TipoModuloABCFuente = 'ACTIVITY'"
  						+ " AND _TipoModuloABCDestino = 'COSTOBJECT'";

				Logger.info(sql);
		 		result = this._queryWithManyResults(sql);
		
		//	}
		//}*/

		return result;
	}


	/**
	 * Obtener datos multidimension
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getMultiDimensionViewData(
			String modelo, String padre, String periodo, String scenario,
			String tipo, String padreDim) {

		Logger.info("Getting Admin View Data");

		String bd = modelo;
		String sql = "";

		// Si no tiene padre, o sea, es nodo raiz
		if (padre.equals("")) {

			sql = "select MD.ID, Padre_ID, MD.Nombre as DisplayName, MD.ID as DisplayRef, Padre_ID as Padre,"
					+ " _IDDimension as DimName, D.Nombre as DimRef, nivel as Nivel, "
					+ " MD._NombreModelo as Modelo, OD._TipoModuloABC as tipoModulo,"
					+ " MD._IDPeriodo as IDPeriodo, MD._IDEScenario as IDEscenario  "
					+ " from  "
					+ bd+ ".Vista_miembrodimension as MD,  "
					+ bd+ ".ordendimension as OD, "
					+ bd+ ".dimension as D"
					+ " where MD._IDDimension = OD._ID  "
					+ " AND OD._TipoModuloABC = '"
					+ tipo
					+ "'"
					+ " AND MD._IDPeriodo = '"
					+ periodo
					+ "'"
					+ " AND MD._IDEScenario = '"
					+ scenario
					+ "'"
					+ " AND (Padre_ID is null or Padre_ID = '') "
					+ " AND OD.Jerarquia_Dimension = 1"
					+ " AND D.ID = MD._IDDimension"
					+ " AND D._IDPeriodo = MD._IDPeriodo"
					+ " AND D._IDEscenario = MD._IDEscenario"
					+ " order by OD.Jerarquia_Dimension , MD.__JerarquiaReferencia";

		} else { // Nodos con padre

			sql = "select MD.ID, Padre_ID, MD.Nombre as DisplayName, MD.ID as DisplayRef, Padre_ID as Padre, "
					+ " _IDDimension as DimName, D.Nombre as DimRef, nivel as Nivel, "
					+ " MD._NombreModelo as Modelo, OD._TipoModuloABC as tipoModulo,"
					+ " MD._IDPeriodo as IDPeriodo, MD._IDEScenario as IDEscenario  "
					+ " from  "
					+ bd+ ".Vista_miembrodimension  as MD,  "
					+ bd+ ".ordendimension as OD, "
					+ bd+ ".dimension as D "
					+ " where MD._IDDimension = OD._ID  "
					+ " AND OD._TipoModuloABC = '"	+ tipo	+ "'"
					+ " AND MD._IDPeriodo = '"	+ periodo	+ "'"
					+ " AND MD._IDEScenario = '"+ scenario	+ "'"
					+ " AND Padre_ID = '" + padre	+ "'"
					+ " AND MD._IDDimension in("+ " SELECT '"+ padreDim	+ "' "	+ " union "	+ " select _ID from "+ bd	+ ".ordendimension"
					                            + " WHERE _TipoModuloABC = '"+ tipo	+ "' and Jerarquia_Dimension = "
					                            + " ( SELECT Jerarquia_Dimension + 1 from "	+ bd+ ".ordendimension "
					                            + "   WHERE _TipoModuloABC = '"	+ tipo	+ "' and _ID = '"+ padreDim	+ "')"
					                            + " ) "
					+ " AND D.ID = MD._IDDimension"
					+ " AND D._IDPeriodo = MD._IDPeriodo"
					+ " AND D._IDEscenario = MD._IDEscenario"
					+ " order by OD.Jerarquia_Dimension , MD.__JerarquiaReferencia";

		}
		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	/**
	 * Obtener Periodo/Escenario
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getPeriodoEscenarioData(
			String modelo, String modeloID) {
		Logger.info("Obteniendo PeriodScenario Data");

		String bd = modelo; // getDBName(modelo);
		String sql = "";

		sql = "select _IDPeriodo + ' / ' + _IDEscenario as periodoScenario, _IDPeriodo + ' / ' + _IDEscenario as periodoScenarioID "
				+ " from  "
				+ bd
				+ ".modelo"
				+ " where _nombreModelo = '"
				+ modeloID + "'";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	/**
	 * Obtener Modelos
	 *
	 * @return
	 */
	public static ArrayList<HashMap<String, String>> getModelosData() {

		Logger.info("Obteniendo Modelos Data");

		//Modelos actuales al 01-02-2016

		String sql = " select Nombre as model, 'TMP_CS' as modelID from TMP_CS.modelo"
				+ " union"
				+ "	select Nombre as model, 'TMP_RED' as modelID from TMP_RED.modelo"
				+ "	union"
				+ "	select Nombre as model, 'TMP_COM' as modelID from TMP_COM.modelo";		
		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = _queryWithManyResults(sql);

		return result;
	}


	/**
	 * Obtener Modelos usados al 26-01-2016
	 *
	 * @return
	 */
	public static ArrayList<HashMap<String, String>> getModelosDataNew() {

		Logger.info("Obteniendo Modelos Data NEW");

		String sql = "";

		sql = " select Nombre as model, 'TMP_CS' as modelID from TMP_CS.modelo"
				+ " union"
				+ "	select Nombre as model, 'TMP_RED' as modelID from TMP_RED.modelo"
				+ "	union"
				+ "	select Nombre as model, 'TMP_COM' as modelID from TMP_COM.modelo";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = _queryWithManyResults(sql);

		return result;
	}




	/**
	 * Obtener Conductores
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getConductoresData(String modelo,
			String periodo, String escenario, String bd) {

		Logger.info("Obteniendo Modelos Data");

		String sql = "";

		sql = "SELECT" + " Nombre as conductor, Nombre as conductorID"
				+ " FROM " + bd + ".Conductor" + " where"
				+ " _NombreModelo = '" + modelo + "'" + " AND _IDPeriodo = '"
				+ periodo + "'" + " AND _IDEscenario = '" + escenario + "'";

		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	/**
	 * Obtener datos multidimension
	 *
	 * @return
	 */
	public HashMap<String, String> getMultiDimensionViewNode(String modelo,
			String padre, String periodo, String scenario, String tipo) {
		Logger.info("Obteniendo getMultiDimensionViewNode");

		String bd = modelo;
		String sql = "";

		sql = "select ID, Padre_ID, Nombre as DisplayName,  '0' as Costo,  "
				+ " _IDDimension as DimName, Padre_ID as Padre, 'example' as DimRef, '0' as DQF, nivel as Nivel, "
				+ " 'Example' as DriverName, 'example' as IntsctnRef, MD._NombreModelo as Modelo, OD._TipoModuloABC as tipoModulo,"
				+ " MD._IDPeriodo as IDPeriodo, MD._IDEScenario as IDEscenario  "
				+ " from  "
				+ bd
				+ ".Vista_miembrodimension as MD,  "
				+ bd
				+ ".ordendimension as OD"
				+ " where MD._IDDimension = OD._ID"
				+ " AND OD._TipoModuloABC = '" + tipo + "'"
				+ " AND MD._IDPeriodo = '" + periodo + "'"
				+ " AND MD._IDEScenario = '" + scenario + "'" + " AND ID = '"
				+ padre + "'"
				+ " order by OD.Jerarquia_Dimension , MD.__JerarquiaReferencia";

		Logger.info(sql);

		HashMap<String, String> result = this._queryWithResult(sql);
		return result;
	}

	/**
	 * Obtener datos para la vista de Administrador
	 *
	 * @return
	 */
	public HashMap<String, String> getAdminViewRowTemporal(String modelo,
			String padre, String periodo, String scenario, String tipo) {
		Logger.info("Getting Admin View Row Temporal");

		String bd = modelo;
		String sql = "";

		sql = "select ID, Padre_ID, Nombre as DisplayName,  '0' as Costo,  "
				+ " _IDDimension as DimName, 'example' as DimRef, '0' as DQF, nivel as Nivel, "
				+ " 'Example' as DriverName, 'example' as IntsctnRef, MD._NombreModelo as Modelo, OD._TipoModuloABC as tipoModulo,"
				+ " MD._IDPeriodo as IDPeriodo, MD._IDEScenario as IDEscenario  "
				+ " from  "
				+ bd
				+ ".Vista_miembrodimension as MD,  "
				+ bd
				+ ".ordendimension as OD"
				+ " where MD._IDDimension = OD._ID"
				+ " AND OD._TipoModuloABC = '" + tipo + "'"
				+ " AND MD._IDPeriodo = '" + periodo + "'"
				+ " AND MD._IDEScenario = '" + scenario + "'" + " AND ID = '"
				+ padre + "'"
				+ " order by OD.Jerarquia_Dimension , MD.__JerarquiaReferencia";

		Logger.info(sql);
		HashMap<String, String> result = this._queryWithResult(sql);

		return result;
	}

	/**
	 * Obtener data DQF
	 *
	 * @return
	 */
	public HashMap<String, String> getDQFSum(String modelo, String periodo, String ceco, String conductor) {
		Logger.info("Obteniendo DQFSum");

		String bd = getModelNameAsDB(modelo);
		
		String sql = " SELECT SUM(DQF) as Total from ( SELECT distinct dimensiondestino, dqf from " + bd +
				      ".AsignacionTemporal " + " WHERE " +
				      " _IDPeriodo = '"+ periodo+"' " +
				      " and _TipoModuloABCFuente = 'RESOURCE'" +
				      " and _NombreConductor = '"+ conductor + "' " +
				      " and _TipoModuloABCDestino = 'ACTIVITY'" +
				      " and FuenteDimMemberRef2 = '"+ ceco +"'" +
				      " and FuenteDimMemberRef3 in " +
				      "                 (select distinct GrupoCuenta from TMP_CS.EquivalenciaGC where " +
				      "                  _IDconductor = '"+ conductor +"') ) as tabla1" ;
	
		Logger.info(sql);
		HashMap<String, String> result = this._queryWithResult(sql);
		
		return result;
	}



	/**
	 * Obtener data DQF
	 *
	 * Actualizado al 04-03-2016
	 * 
	 * @return
	 */
	public HashMap<String, String> getDQFSumEstadistico(String dimFuente) {
		
		Logger.info("Obteniendo DQFSumEstadistico");
			
		String sql = "SELECT IFNULL(SUM(DQF),0) as Total FROM TMP_COM.AsignacionTemporal"
					+" WHERE DimensionFuente = '"+dimFuente+"'"
					+" AND _TipoModuloABCFuente = 'ACTIVITY'"
					+" AND _TipoModuloABCDestino = 'COSTOBJECT'";
	
		Logger.info(sql);
		
		return this._queryWithResult(sql);
	}



	/**
	 * Obtener id del usuario
	 *
	 * @return
	 */
	public ArrayList<HashMap<String, String>> getUsuarioId(String usuario) {
		
		Logger.info("Obteniendo ID del usuario");
		String sql = "select id from SASWeb.Usuario where usuario = '" 
				+ usuario + "'";

		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this._queryWithManyResults(sql);
		return result;
	}


    public ArrayList<HashMap<String, String>> getPeriodoTable() {
		Logger.info("Obteniendo Periodo desde tabla");

		String sql = "SELECT DISTINCT anho FROM SASWeb.periodo where anho"
					 + " <= (SELECT substring(CONVERT(VARCHAR(10),GETDATE(),101),7,4))";

		Logger.debug(sql);
		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}

	
	public ArrayList<HashMap<String, String>> getEstadisticos(int estatus) {
		
		String sql = "SELECT * FROM SASWeb.estadistico_com"
					+ " WHERE estatus = "+estatus;

		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}


	public HashMap<String, String> getEstadisticoActual(String region, String proceso, String unidad, String segmento, int estatus) {
		
		Logger.info("Obteniendo informacion del estadistico actual");

		String sql = "SELECT * FROM SASWeb.estadistico_com"
					+ " WHERE region = '"+region+"'"
					+ " AND proceso = '"+proceso+"'"
					+ " AND unidad = '"+unidad+"'"
					+ " AND segmento = '"+segmento+"'"
					+ " AND estatus = "+estatus;
		
		Logger.info(sql);

		return this._queryWithResult(sql);

	}	


	public ArrayList<HashMap<String, String>> getEncuestasNoAsignadas() {
		Logger.info("Obteniendo Encuestas No Asignadas");

		String sql = "SELECT id, nombre FROM SASWeb.Encuesta"
						+ " WHERE id NOT IN (SELECT DISTINCT encuesta_id"  
						+ " FROM SASWeb.PermisoEncuesta)";

		//Logger.debug(sql);
		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this
				._queryWithManyResults(sql);

		return result;
	}


	public static String getModelNameAsDB(String modelName) {

		String modelo = null;
		// Obtenemos bd modelo basado en el nombre del Modelo
		ArrayList<HashMap<String, String>> allModels;
		
		allModels = getModelosData();

		for (HashMap<String, String> hashMap : allModels) {
			
			if (hashMap.get("model").equals(modelName)) {
				modelo = hashMap.get("modelID");
				break;
			}
		}
		return modelo;
	}

	//Obtener modelos usados al 26-01-2016
	public static String getModelNameAsDBNew(String modelName) {

		String modelo = null;
		// Obtenemos bd modelo basado en el nombre del Modelo
		ArrayList<HashMap<String, String>> allModels;
		
		allModels = getModelosDataNew();

		for (HashMap<String, String> hashMap : allModels) {
			
			if (hashMap.get("model").equals(modelName)) {
				modelo = hashMap.get("modelID");
				break;
			}
		}
		return modelo;
	}

	public HashMap<String, String> getDQFSum(String bd, int idPermiso,
			int idPregunta) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * Obtener Usuarios revisores
	 * 
	 */
	public ArrayList<HashMap<String, String>> getRevisores() {
		
		Logger.info("Obteniendo Usuarios revisores");
		
		String sql =  "SELECT A.*"
  						+ " FROM SASWeb.Usuario A,SASWeb.Rol B,"
  						+ " SASWeb.Usuario_Rol C"
 						+ " WHERE B.id = 3 AND B.id = C.rol_id"
   						+ " AND C.Usuario_id = A.id"
   						+ " ORDER BY nombre ASC";
			   		
		Logger.info(sql);
		ArrayList<HashMap<String, String>> result = this._queryWithManyResults(sql);

		return result;
	}

}
