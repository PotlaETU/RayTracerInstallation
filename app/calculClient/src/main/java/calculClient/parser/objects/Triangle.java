package calculClient.parser.objects;

import calculClient.parser.scene.SceneObjects;
import calculClient.triplet.Color;
import calculClient.triplet.Point;
import calculClient.triplet.Vector;


/**
 * The type Triangle.
 */
public class Triangle extends SceneObjects {
    private final Point a;
    private final Point b;
    private final Point c;
    private Vector normal;

    /**
     * Instantiates a new Triangle.
     *
     * @param vertex1       the vertex 1
     * @param vertex2       the vertex 2
     * @param vertex3       the vertex 3
     * @param diffuseColor  the diffuse color
     * @param specularColor the specular color
     * @param shininess     the shininess
     * @param ambient       the ambient
     */
    public Triangle(Point vertex1, Point vertex2, Point vertex3, Color diffuseColor, Color specularColor, int shininess, Color ambient) {
        super(diffuseColor, specularColor, ambient, shininess);
        this.a = vertex1;
        this.b = vertex2;
        this.c = vertex3;
    }

    /**
     * Get p triangle point.
     *
     * @return the point
     */
    public Vector getNTriangle(){
        return b.sub(a).multiply(c.sub(a).length()).normalize();
    }

    public boolean isInTriangle(Point p){
        return b.sub(a).multiply(p.sub(a).length()).scalarProduct(getNTriangle())>=0 && c.sub(b).multiply(p.sub(b).length()).scalarProduct(getNTriangle())>=0 && a.sub(c).multiply(p.sub(c).length()).scalarProduct(getNTriangle())>=0;
    }
}
