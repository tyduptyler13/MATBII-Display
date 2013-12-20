import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Trial extends VBox{

	private Node content;
	private String name;
	private ProgressBar progress;
	private ContextMenu menu;
	
	public Trial(String name, File[] files){
		super();
		
		this.name = name;
		
		Text title = new Text(name);
		
		progress = new ProgressBar();
		
		getChildren().addAll(title, progress);
		
		//Context Menu
		menu = new ContextMenu();
		MenuItem saveButton = new MenuItem("Save Statistics");
		saveButton.setOnAction(new SaveEventHandle());

	}
	
	private void setupTask(){
		
	}
	
	private class SaveEventHandle implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent evt) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
