package sample;

import complex.ComplexNumber;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Controller {
    @FXML private Canvas canvas;
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
        /*Rectangle clip = new Rectangle(800,600);
        clip.setLayoutX(0);
        clip.setLayoutY(0);
        canvas.setClip(clip);*/
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
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
            @Override
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

        resetCanvas();
    }
    void drawSet(int depth){
        isDrawing = true;
//        x: [-1.5, 2]
//        y: [-1  , 1]
        double width = canvas.getWidth(), height = canvas.getHeight();

        for(double x = 0.0; x < width; x += 1.0){
            for(double y = 0.0; y < height; y += 1.0){
                drawMandelbrotPoint(x,y,depth);
//                drawJuliaPoint(x,y,depth,-0.4,0.6);
            }
            System.out.println(100 * x/width + "% done");
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
    void drawMandelbrotPoint(double x, double y, int depth){
        double px, py, zx=0, zy=0;
        double dx, dy;
        ComplexNumber z = new ComplexNumber(0,0);
        dx = (xmax - xmin)/canvas.getWidth();
        dy = (ymax - ymin)/canvas.getHeight();
        Color color = Color.color(0,0,0);
        px = x*dx + xmin;
        py = y*dy + ymin;
        ComplexNumber c = new ComplexNumber(px, py);
        for(int i = 0; i < depth; i++){
            //Math do do it before I made a wrapper class
            double tempx = zx, tempy = zy;
            zx = Math.pow(tempx,2) - Math.pow(tempy, 2) + px;
            zy = 2 * tempx * tempy + py;
            double sqDist = Math.pow(zx, 2) + Math.pow(zy, 2);
            /////////////////////////////////////////////
            z = z.multiply(z).add(c);
            if(z.sqMagnitude() >= 4){
                double pct = (double)i/(double)depth;
                color = Color.color(pct, pct*pct, (Math.pow(2*pct-1, 2)));
                break;
            }
        }
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
    void drawPoint(double x, double y){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.fillRect(x, y, 1,1);
    }
    void drawPoint(double x, double y, Paint p){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Paint temp = gc.getFill();
        gc.setFill(p);
        drawPoint(x, y);
        gc.setFill(temp);
    }
}
