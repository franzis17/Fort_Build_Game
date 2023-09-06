package fortbuild;

import javafx.application.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class UserInterface
{
    private Label scoreLabel;
    private TextArea log;
    
    private Player player;
    
    public UserInterface(Player player)
    {
        this.player = player;
    }
    
    public void show(Stage stage)
    {
        // Setup UI
        stage.setTitle("Example App (JavaFX)");
        JFXArena arena = new JFXArena();
        arena.addListener((x, y) ->
        {
            System.out.println("Arena click at (" + x + "," + y + ")");
        });
        
        ToolBar toolbar = new ToolBar();
        // Button btn1 = new Button("My Button 1");
        // Button btn2 = new Button("My Button 2");
        scoreLabel = new Label("Score: 999");
        // toolbar.getItems().addAll(btn1, btn2, label);
        toolbar.getItems().addAll(scoreLabel);
        
        // btn1.setOnAction((event) ->
        // {
        //     System.out.println("Button 1 pressed");
        // });
                    
        log = new TextArea();
        log.appendText("Hello\n");
        log.appendText("World\n");
        
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(arena, log);
        arena.setMinWidth(300.0);
        
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);
        
        Scene scene = new Scene(contentPane, 800, 800);
        
        stage.setScene(scene);
        
        // On window close, stop all background running threads
        stage.setOnCloseRequest(event -> {
            player.stopScoreCount();
        });
        
        stage.show();
        
        // Setup assets
        
    }

    public void setScore(int score)
    {
        Platform.runLater(() -> {
            scoreLabel.setText("Score = " + score);
        });
    }
}
