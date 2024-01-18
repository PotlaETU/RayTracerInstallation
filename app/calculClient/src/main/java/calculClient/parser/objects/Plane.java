package calculClient.parser.objects;

import calculClient.parser.scene.SceneObjects;
import calculClient.triplet.Color;
import calculClient.triplet.Point;
import calculClient.triplet.Triplet;
import calculClient.triplet.Vector;


/**
 * The type Plane.
 */
public class Plane extends SceneObjects {
    private final Vector normal;

    private final Point positionPoint;

    /**
     * Instantiates a new Plane.
     *
     * @param positionPoint the position
     * @param normal        the normal
     * @param diffuseColor  the diffuse color
     * @param specularColor the specular color
     * @param shininess     the shininess
     */
    public Plane(Point positionPoint, Vector normal, Color diffuseColor, Color specularColor, int shininess, Color ambient) {
        super(diffuseColor, specularColor, ambient, shininess);
        this.positionPoint = positionPoint;
        this.normal = normal.normalize();
    }

    /**
     * Caclul p point. To intersect of a point.
     *
     * @param d        the d
     * @param lookFrom the look from
     * @return the point
     */
    public Point caclulP(Vector d, Triplet lookFrom){
        Point p = new Point(positionPoint.getCoor());
        double up = p.sub(new Point(lookFrom)).scalarProduct(new Vector(normal.getCoor()));
        double down = d.scalarProduct(new Vector(normal.getCoor()));
        if(down == 0){
            return null;
        }
        double t =  up/down;
        return new Point(lookFrom.add(d.multiply(t).getCoor()));
    }
}
