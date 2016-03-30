import models.User;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.mvc.Router;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

	public void doJob() {
		// Check if the database is empty
		if (User.count() == 0) {
			Fixtures.loadModels("initial-data.yml");
		}

		Play.ctxPath = "/SASWeb";
		Router.load(Play.ctxPath);
	}

}