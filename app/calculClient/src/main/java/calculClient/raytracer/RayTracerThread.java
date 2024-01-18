package calculClient.raytracer;

import calculClient.parser.objects.Sphere;
import calculClient.triplet.Color;
import calculClient.triplet.Point;
import calculClient.triplet.Vector;

public class RayTracerThread extends Thread {
    private final RayTracer rayTracer;
    private final int startRow;
    private final int endRow;

    private Sphere currentSphere;

    /**
     * Instantiates a new Ray tracer thread.
     *
     * @param rayTracer the ray tracer instance
     * @param startRow  the starting row for computation
     * @param endRow    the ending row for computation
     */
    public RayTracerThread(RayTracer rayTracer, int startRow, int endRow) {
        this.rayTracer = rayTracer;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    public void setCurrentSphere(Sphere currentSphere) {
        this.currentSphere = currentSphere;
    }

    /**
     * Gets t.
     *
     * @param d the d
     * @return t
     */
    public double getT(Vector d) {
        for(Sphere sphere : rayTracer.getScene().getSphere()){
            Point sphereVector = sphere.getPosition();
            double b = 2*rayTracer.getCamera().getLookFrom().sub(sphereVector).scalarProduct(d);
            double c = rayTracer.getCamera().getLookFrom().sub(sphereVector).scalarProduct(rayTracer.getCamera().getLookFrom().sub(sphereVector)) - Math.pow(sphere.getRadius(), 2);
            double delta = Math.pow(b,2) - 4 * c;


            if (delta==0){
                setCurrentSphere(sphere);
                return -b/2 ;
            }
            else if(delta>0){
                double t1 = -b + Math.sqrt(delta)/2;
                double t2 = -b - Math.sqrt(delta)/2;
                if (t2>0) {
                    setCurrentSphere(sphere);
                    return t2;
                }else if(t1>0){
                    setCurrentSphere(sphere);
                    return t1;
                }
            }
        }
        return -1;
    }

    /**
     * Get p vector.
     *
     * @param i the
     * @param j the j
     * @return the vector
     */
    public Point getP(int i, int j){
        Vector d = rayTracer.getD(i, j);
        return d.multiply(getT(d)).add(rayTracer.getScene().getCamera().getLookFrom());
    }

    @Override
    public void run() {
        try {
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < rayTracer.getImgHeight(); j++) {
                    Vector d = rayTracer.getD(i, j);
                    double t = getT(d);
                    Color color = new Color(0, 0, 0);
                    if (t != -1) {
                        color = rayTracer.calculColorLambert(currentSphere, getP(i,j));
                    }
                    rayTracer.getImg().setRGB(i, j, new java.awt.Color(
                            (float) color.getCoor().getX(),
                            (float) color.getCoor().getY(),
                            (float) color.getCoor().getZ()).getRGB());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}