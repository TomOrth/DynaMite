import java.util.*;
import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class dynaGui extends Application {

    private static double SCENE_WIDTH = 800;
    private static double SCENE_HEIGHT = 600;
    static List<Double> xcoord;
    static List<Double> ycoord;
    Canvas canvas;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inital window setupxw
        Button btn = new Button();
        btn.setText("Begin tracing");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                trace(primaryStage);
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        Scene scene = new Scene(root, 200, 150);

        primaryStage.setTitle("Dynamite");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void trace(Stage primaryStage) {
        xcoord = new ArrayList<Double>();
        ycoord = new ArrayList<Double>();

        // Initiate bluetooth

        // Send start signal

        // Read in all sensor data

        // Process sensor data into coordinates and add them to arraylists

        xcoord.add(10.0);
        ycoord.add(10.0);
        xcoord.add(400.0);
        ycoord.add(400.0);

        // Create button to start animation
        Button btn = new Button();
        btn.setText("Begin animation");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                runAnimation();
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        Scene scene = new Scene(root, 200, 150);
        primaryStage.setScene(scene);
    }

    private void runAnimation() {
        Stage animationStage = new Stage(); 
        Pane root = new Pane();
        Path path = createPath();
        canvas = new Canvas(SCENE_WIDTH,SCENE_HEIGHT);
        root.getChildren().addAll(path, canvas);

        animationStage.setScene(new Scene(root, SCENE_WIDTH, SCENE_HEIGHT));
        animationStage.show();

        Animation animation = createPathAnimation(path, Duration.seconds(6));
        animation.play();
    }



    private Path createPath() {
        Path path = new Path();
        path.setStroke(Color.WHITE);
        path.setStrokeWidth(1);

        // Define movement between coordinates
        path.getElements().add(new MoveTo(xcoord.get(0), ycoord.get(0)));
        for (int i = 1; i < xcoord.size(); i++) {
            path.getElements().add(new LineTo(xcoord.get(i), ycoord.get(i)));
        }

        return path;
    }

    private Animation createPathAnimation(Path path, Duration duration) {

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // move a node along a path. we want its position
        Circle pen = new Circle(0, 0, 10);

        // create path transition
        PathTransition pathTransition = new PathTransition( duration, path, pen);
        pathTransition.currentTimeProperty().addListener( new ChangeListener<Duration>() {

            Location oldLocation = null;

            //Draw a line from the old location to the new location
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {

                // skip starting at 0/0
                if( oldValue == Duration.ZERO)
                    return;

                // get current location
                double x = pen.getTranslateX();
                double y = pen.getTranslateY();

                // initialize the location
                if( oldLocation == null) {
                    oldLocation = new Location();
                    oldLocation.x = x;
                    oldLocation.y = y;
                    return;
                }

                // draw line
                gc.setStroke(Color.BLUE);
                gc.setFill(Color.YELLOW);
                gc.setLineWidth(4);
                gc.strokeLine(oldLocation.x, oldLocation.y, x, y);

                // update old location with current one
                oldLocation.x = x;
                oldLocation.y = y;
            }
        });

        return pathTransition;
    }

    public static class Location {
        double x;
        double y;
    }
}
