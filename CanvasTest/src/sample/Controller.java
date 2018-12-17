package sample;

import complex.ComplexNumber;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Controller {
    @FXML private Canvas canvas;
    @FXML private Label iterLabel;
    @FXML private Slider iterSlider;
    @FXML private ProgressBar progressBar;
    private double xmin, ymin, xmax, ymax;
    private long timeSinceLastScroll;
    private boolean isDrawing;
    private int iters;
    public void initialize(){
        iters = 1000;
        xmin = -2.5;
        xmax = 1.0;
        ymin = -1.0;
        ymax = 1.0;

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if(!isDrawing && event.getButton().equals(MouseButton.PRIMARY)){
                    System.out.println("Before");
                    System.out.printf("xmin: %f xmax %f\nymin: %f ymax: %f\n", xmin, xmax, ymin, ymax);
                    double x = event.getX();
                    double y = event.getY();
                    double x0, y0, dx, dy, changex, changey;
                    dx = (xmax - xmin)/canvas.getWidth();
                    dy = (ymax - ymin)/canvas.getHeight();
                    x0 = canvas.getWidth()/2;
                    y0 = canvas.getHeight()/2;
                    System.out.printf("x: %f y: %f\n", x, y);
                    System.out.printf("x0: %f y0: %f\n", x0, y0);
                    changex = dx * (x-x0);
                    changey = dy * (y-y0);
                    System.out.println("changex = " + changex);
                    System.out.println("changey = " + changey);
                    xmax += changex;
                    xmin += changex;
                    ymin += changey;
                    ymax += changey;
                    System.out.println("After");
                    System.out.printf("xmin: %f xmax %f\nymin: %f ymax: %f\n", xmin, xmax, ymin, ymax);
                    System.out.printf("x0: %f y0: %f\n", (-xmin/dx), -ymin/dx);
                    drawSet(iters);
                }
                else if(!isDrawing && event.getButton().equals(MouseButton.SECONDARY)){
                    System.out.println("Drawing");
                    resetCanvas();
                    drawSet(iters);
                }


            }
        });
        canvas.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            public void handle(ScrollEvent event) {
                if(!isDrawing){
                    double distx = Math.abs(xmax - xmin)/4;
                    double disty = Math.abs(ymax - ymin)/4;
                    if(event.getDeltaY() < 0){
                        distx *= -1;
                        disty *= -1;
                    }
                    xmax -= distx;
                    xmin += distx;
                    ymax -= disty;
                    ymin += disty;
                    System.out.println("xmax = " + xmax);
                    System.out.println("xmin = " + xmin);
                    System.out.println("ymax = " + ymax);
                    System.out.println("ymin = " + ymin);
                    drawSet(iters);
                }
            }
        });
        iterSlider.setOnKeyTyped((event) -> {
            System.out.println("event.getCharacter() = " + event.getCharacter());
            if(event.getCharacter().equals(" ")){
                drawSet(iters);
            }
        });
        iterSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            iters = newValue.intValue();
            iterLabel.setText(iters+"");
        });
        iters = (int)iterSlider.getValue();
        iterLabel.setText(iters + "");
        resetCanvas();
    }
    void drawSet(final int depth) {
        isDrawing = true;
//        x: [-1.5, 2]
//        y: [-1  , 1]
        final double width = canvas.getWidth(), height = canvas.getHeight();

        for (double x = 0.0; x < width; x += 1.0) {
            for (double y = 0.0; y < height; y += 1.0) {
                drawMandelbrotPoint(x, y, depth);
            }
            System.out.printf("%.3f%% done\n", 100 * x / width);
        }
        isDrawing = false;
    }
    void drawJuliaPoint(double x, double y, int depth, double cx, double cy){
        double zx=0, zy=0;
        double dx, dy;
        Color color = Color.color(0,0,0);
        dx = (xmax - xmin)/canvas.getWidth();
        dy = (ymax - ymin)/canvas.getHeight();
        zx = dx * x;
        zy = dy*y;
        ComplexNumber c = new ComplexNumber(cx, cy);
        ComplexNumber z = new ComplexNumber(zx, zy);
        for(int i = 0; i < depth; i++){
            z = z.multiply(z).add(c);
            if(z.sqMagnitude() >= 4){
                double pct = (double)i/(double)depth;
                color = Color.color(pct, pct*pct, (Math.pow(2*pct-1, 2)));
                break;
            }
        }
    }

    Color getMandelbrotColor(double x, double y, int depth){
        double px, py;
        double dx, dy;
        ComplexNumber z = new ComplexNumber(0,0);
        dx = (xmax - xmin)/canvas.getWidth();
        dy = (ymax - ymin)/canvas.getHeight();
        Color color = Color.color(0,0,0);
        px = x*dx + xmin;
        py = y*dy + ymin;
        ComplexNumber c = new ComplexNumber(px, py);
        for(int i = 0; i < depth; i++){
            z = z.multiply(z).add(c);
            if(z.sqMagnitude() >= 4){
                double pct = (double)i/(double)depth;
                color = Color.color(pct, pct*pct, (Math.pow(2*pct-1, 2)));
                break;
            }
        }
        return color;
    }
    void drawMandelbrotPoint(final double x, final double y, final int depth){
        Color color = getMandelbrotColor(x, y, depth);
        drawPoint(x,y,color);
    }
    void resetCanvas(){
        xmin = -2.5;
        xmax = 1.0;
        ymin = -1.0;
        ymax = 1.0;
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        Paint temp = ctx.getFill();
        ctx.setFill(Color.BLUE);
        ctx.fillRect(0,0,canvas.getWidth(), canvas.getHeight());
        ctx.setFill(temp);
    }

    void drawPoint(double x, double y, Color p){
        PixelWriter pw = canvas.getGraphicsContext2D().getPixelWriter();
        pw.setColor((int)x, (int)y, p);
    }
}
