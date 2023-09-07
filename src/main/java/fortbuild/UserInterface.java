package fortbuild;

import javafx.application.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class UserInterface
{
    // UI
    private Label scoreLabel;
    private TextArea log;
    
    private JFXArena arena;

    private Player player;
    
    public UserInterface(Player player)
    {
        this.player = player;
    }
    
    public void show(Stage stage)
    {
        // Setup UI
        stage.setTitle("Example App (JavaFX)");
        arena = new JFXArena();
        System.out.println("Thread name: " + Thread.currentThread());
        
        // On mouse click, create a wall and add it to the wall builder
        arena.addListener((x, y) ->
        {
            System.out.println("Arena click at (" + x + "," + y + ")");
            
            //wallBuilder.enqueue(new Wall(x, y));
            arena.enqueueWall(new Wall(x, y));
        });
        
        ToolBar toolbar = new ToolBar();
        Button testBtn = new Button("Test Button");
        // Button btn2 = new Button("My Button 2");
        scoreLabel = new Label("Score: 999");
        // toolbar.getItems().addAll(btn1, btn2, label);
        toolbar.getItems().addAll(scoreLabel, testBtn);

        testBtn.setOnAction((event) ->
        {
            testRobotMovement();
        });

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
        stage.setOnCloseRequest(event -> 
        {
            player.stopScoreCount();
        });
        
        stage.show();
    }

    public void setScore(int score)
    {
        Platform.runLater(() ->
        {
            scoreLabel.setText("Score = " + score);
        });
    }
    
    /**
     * Only log the following events: Each time a robot is created, the player creates a wall,
     * or a wall is impacted
     */
    public void setLog(String text)
    {
        Platform.runLater(() ->
        {
            log.appendText(text);
        });
    }
    
    public void testRobotMovement()
    {
        System.out.println("Testing Robot movement");
        arena.setRobotPosition(8, 8);
    }
}
