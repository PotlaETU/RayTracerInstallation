package calculClient.raytracer;

import calculClient.calculColor.LambertColorCal;
import calculClient.parser.Camera;
import calculClient.parser.objects.Sphere;
import calculClient.parser.scene.Scene;
import calculClient.triplet.Color;
import calculClient.triplet.Point;
import calculClient.triplet.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The type Ray tracer.
 */
public class RayTracer {
    private final Scene scene;

    private final LambertColorCal lambertColorCal =new LambertColorCal();

    private final int imgHeight;

    private final int imgWidth;

    private final Camera camera;

    private final BufferedImage img;

    /**
     * Instantiates a new Ray tracer.
     *
     * @param scene the scene
     */
    public RayTracer(Scene scene) {
        this.scene = scene;
        this.imgHeight = scene.getHeight();
        this.imgWidth = scene.getWidth();
        this.camera = scene.getCamera();
        img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
    }


    /**
     * Get pixel width double.
     *
     * @return the double
     */
    public double getPixelWidth(){
        return  getRealWidth()/imgWidth;
    }

    /**
     * Get pixel height double.
     *
     * @return the double
     */
    public double getPixelHeight(){
        return getRealHeight()/imgHeight;
    }

    /**
     * Get real height double.
     *
     * @return the double
     */
    public double getRealHeight(){
        return 2*Math.tan(camera.getFovR()/2);
    }

    /**
     * Get real width double.
     *
     * @return the double
     */
    public double getRealWidth(){
        return imgWidth*getPixelHeight();
    }


    public int getImgHeight() {
        return imgHeight;
    }

    public BufferedImage getImg() {
        return img;
    }

    public Camera getCamera() {
        return camera;
    }

    public Color calculColorLambert(Sphere currentSphere, Point p){
        return lambertColorCal.calculateColor(currentSphere, scene, p);
    }

    /**
     * Get d vector.
     *
     * @param i the
     * @param j the j
     * @return the vector
     */
    public Vector getD(int i, int j){
        double a = -getRealWidth()/2 + (i+0.5)*getPixelWidth();
        double b = getRealHeight()/2 - (j+0.5)*getPixelHeight();
        return getCamera().getU().multiply(a).add(getCamera().getV().multiply(b)).sub(getCamera().getW()).normalize();
    }

    /**
     * View.
     */
    public byte[] view() {
        try {
            int nbThreads = Runtime.getRuntime().availableProcessors();
            Thread[] threads = new Thread[nbThreads];
            int rowsPerThread = imgWidth / nbThreads;

            for (int i = 0; i < nbThreads; i++) {
                int startRow = i * rowsPerThread;
                int endRow = (i + 1) * rowsPerThread;
                if (i == nbThreads - 1) {
                    endRow = imgHeight;
                }
                threads[i] = new RayTracerThread(this, startRow, endRow);
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                ImageIO.write(img, "png", baos);
                baos.flush();
                return baos.toByteArray();
            } finally {
                baos.close();
            }
        }
        catch (IOException e){
            System.err.println("Erreur lors de la création du fichier : "+e.getMessage());
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Scene getScene() {
        return scene;
    }
}
