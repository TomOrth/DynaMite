package dynagui;

import java.util.*;
import java.util.concurrent.*;
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
import com.fazecast.jSerialComm.*;
import java.io.PrintWriter;
import java.lang.Math;

public class DynaGui extends Application {

    private static double SCENE_WIDTH = 800;
    private static double SCENE_HEIGHT = 600;
    static List<Double> xcoord;
    static List<Double> ycoord;
    static SerialPort port;
    static int inProg = 0;
    Canvas canvas;
    static double count;

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
        String robotPort = "/dev/cu.Makeblock-ELETSPP";
        port = SerialPort.getCommPort(robotPort);
        if(port.openPort()) {
            System.out.println("Successfully opened the port.");
        } else {
            System.out.println("Unable to open the port.");
            return;
        }
        PrintWriter output = new PrintWriter(port.getOutputStream());

        // Create button to end trace
        Button btn = new Button();
        btn.setText("End Tracing");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                inProg = 0;
                
                // Send end signal
                output.print("e");
                output.flush();
                
                // Create button to end animation
                Button btn2 = new Button();
                btn2.setText("Begin Animation");
                btn2.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        runAnimation();
                    }                 
                });
                StackPane root2 = new StackPane();
                root2.getChildren().add(btn2);
                Scene scene2 = new Scene(root2, 200, 150);
                primaryStage.setScene(scene2);     
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        Scene scene = new Scene(root, 200, 150);
        primaryStage.setScene(scene);
        
        // Send start signal
        output.print("s");
        output.flush();
        
        inProg = 1;
        
        // Read in all sensor data
        count = 0;
        xcoord.add(0.0);
        
        //convert 400 into variable that represents robot
        ycoord.add(250.0);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(DynaGui::readData, 0, 200, TimeUnit.MILLISECONDS);
    }
    
    private static void readData() {
        Scanner data = new Scanner(port.getInputStream());
        while(data.hasNextLine() && inProg == 1) {
            String dataEntry = "";
            try{
                dataEntry = data.nextLine();
                if (!dataEntry.equals("")) {
                    System.out.println(dataEntry);
                    xcoord.add(++count);
                    ycoord.add(250.0 + Double.parseDouble(dataEntry.split(" ")[0]) / 2);
                }
            }catch(Exception e){}
        }
        if (inProg == 0) {
            port.closePort();
        }
    }

    private void runAnimation() {
        Stage animationStage = new Stage(); 
        Pane root = new Pane();
        Path path = createPath();
        Path rPath = createRoboPath();
        canvas = new Canvas(SCENE_WIDTH,SCENE_HEIGHT);
        root.getChildren().addAll(path, rPath, canvas);

        animationStage.setScene(new Scene(root, SCENE_WIDTH, SCENE_HEIGHT));
        animationStage.show();

        Animation wall = createPathAnimation(path, Duration.seconds(3));
        wall.play();
        
        
        Animation robot = createPathAnimation(rPath, Duration.seconds(3));
        robot.play();
    }



    private Path createPath() {
        Path path = new Path();
        path.setStroke(Color.WHITE);
        path.setStrokeWidth(.2);
        
        dataCorrection();

        // Define movement between coordinates
        path.getElements().add(new MoveTo(xcoord.get(0), ycoord.get(0)));
        for (int i = 1; i < xcoord.size(); i++) {
            path.getElements().add(new LineTo(xcoord.get(i), ycoord.get(i)));
        }

        return path;
    }
    
    private Path createRoboPath() {
        Path path = new Path();
        path.setStroke(Color.WHITE);
        path.setStrokeWidth(.2);

        // Define movement between coordinates
        path.getElements().add(new MoveTo(xcoord.get(0), ycoord.get(0)));
        for (int i = 1; i < xcoord.size(); i++) {
            path.getElements().add(new LineTo(xcoord.get(i) , 250.0));
        }

        return path;
    }
    
    private void dataCorrection() {
        double before;
        double after;
        double cur;
        for(int i = 1; i < ycoord.size() - 1; i++) {
            before = ycoord.get(i-1);
            after = ycoord.get (i+1);
            cur = ycoord.get(i);
            if ((Math.abs((cur - before)) > 50 && Math.abs((cur - after)) > 50) || cur > 400) {
                ycoord.remove(i);
                xcoord.remove(i);
                i--;
            }
        }
        ycoord.remove(ycoord.size() - 1);
        xcoord.remove(xcoord.size() - 1); 
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
                gc.setStroke(Color.GREEN);
                gc.setFill(Color.GREEN);
                gc.setLineWidth(3);
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
