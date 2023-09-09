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
    private RobotGenerator robotGenerator;
    
    public UserInterface()
    {
        arena = new JFXArena();
        player = new Player(this);  // Give it UI since it changes the score
        robotGenerator = new RobotGenerator(arena);
    }
    
    public void show(Stage stage)
    {
        System.out.println("UI Thread name: " + Thread.currentThread());

        stage.setTitle("Example App (JavaFX)");

        // On mouse click, create a wall and add it to the wall builder
        arena.addListener((x, y) ->
        {
            System.out.println("Arena click at (" + x + "," + y + ")");
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
        stage.show();

        try
        {
            // Start background threads
            player.startScoreCount();
            robotGenerator.start();
            
            // On window close, stop all background running threads
            stage.setOnCloseRequest(event -> 
            {
                player.stopScoreCount();
                arena.endWallBuilder();
                robotGenerator.stop();
            });
        }
        catch(IllegalStateException ise)
        {
            System.out.println("ERROR: " + ise.getMessage());
        }
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
        // System.out.println("Testing Robot movement");
        // arena.setRobotPosition(8, 8);
    }
}
