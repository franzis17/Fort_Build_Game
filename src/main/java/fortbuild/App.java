package fortbuild;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application
{
    public static void main(String[] args) 
    {
        launch();
    }
    
    @Override
    public void start(Stage stage)
    {
        UserInterface ui = new UserInterface();
        ui.show(stage);
    }
}
