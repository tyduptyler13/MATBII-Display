
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class FileReader {

	public FileReader(Stage s){
		
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose the top directory");
		chooser.showDialog(s);
		
	}
	
}
