import java.io.File;

import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Trial extends VBox{

	private Node content;
	private String name;
	private ProgressBar progress;
	
	public Trial(String name, File[] files){
		super();
		
		this.name = name;
		
		Text title = new Text(name);
		
		progress = new ProgressBar();
		
		getChildren().addAll(title, progress);

	}
	
	private void setupTask(){
		
	}
	
}
