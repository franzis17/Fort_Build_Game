package fortbuild;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application
{
    public static void main(String[] args) 
    {
        launch();
        // TestApp test = new TestApp();
        // test.main();
    }
    
    @Override
    public void start(Stage stage) 
    {
        Player player = new Player();
        WallCoordinator wallCdtr = new WallCoordinator();
        UserInterface ui = new UserInterface(player, wallCdtr);

        ui.show(stage);

        player.setUI(ui);
        player.startScoreCount();
    }
}
