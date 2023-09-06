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
        Player player = new Player();
        UserInterface ui = new UserInterface(player);
        player.setUI(ui);
        
        ui.show(stage);

        player.startScoreCount();
    }
}
