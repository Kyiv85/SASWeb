package gwt.adminsas.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.History;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VStack;

/**
 * Punto de entrada para la aplicación SASWebAdmin
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class SASWebAdmin implements EntryPoint {

	final String initToken = History.getToken();
	private VStack main;

	/**
	 * Punto de entrada de la aplicación
	 */
	public void onModuleLoad() {
		// setup overall layout
		// viewport
		main = new VStack();

		HLayout body = new HLayout();
		body.setWidth100();
		body.setHeight100();

		MainView mainView = new MainView();
		body.addMember(mainView);

		main.addMember(body);
		main.draw();
	}
}
