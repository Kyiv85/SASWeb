package gwt.adminsas.client;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.toolbar.ToolStrip;

/**
 * Barra de titulo e información SASWebAdmin
 * 
 * @author Gerardo Curiel <gcuriel@0269.com.ve>
 * 
 */
public class TitleBar extends ToolStrip {

	protected Label titleItem = new Label();

	/**
	 * Constructor por defecto
	 * 
	 */
	public TitleBar() {

		setHeight(24);
		setWidth100();

		String image = "/SASWeb/app/img/logo.gif";
		Img companyLogo = new Img(image, 230, 71);

		companyLogo.setMargin(2);
		addMember(companyLogo);

		titleItem.setWrap(false);
		titleItem.setAlign(Alignment.CENTER);
		titleItem.setValign(VerticalAlignment.CENTER);
		titleItem.setMargin(20);
		titleItem
				.setStyleName("background: #FAF7F2; color:#111111;	padding: 2px 10px;	"
						+ "font-size: 100%;	-moz-border-radius: 18px; -webkit-border-radius: 18px;	"
						+ "text-decoration: none; font-weight: bolder;");

		addMember(titleItem);
		draw();
	}

}
