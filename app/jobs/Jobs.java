package jobs;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;

public class Jobs {

	private String filename;
	private String periodo;
	private String escenario;

	private Result result = null; 

	public Jobs(String filename) {
		this.filename = filename;
	}

	public Jobs(String filename, String periodo, String escenario) {
		this.filename = filename;
		this.periodo = periodo;
		this.escenario = escenario;
	}

	/**
	 * 
	 * Ejecuta los parametros de job
	 * 
	 */
	public boolean runDimensionRelacionJob() {
		try {

			KettleEnvironment.init();

			JobMeta jobMeta = new JobMeta(filename, null);

			Job job = new Job(null, jobMeta);
			job.start(); // Ejecuta el job seleccionado.

			job.waitUntilFinished(); // The bookkeeping...log

			result = job.getResult();
			System.out.println("Cantidad de errores:" + result.getNrErrors());

			return result.getNrErrors() == 0;

		} catch (KettleException e) {
			System.out.println("Imprimiendo: " + e);
			return false;
		}

	}
}
