package calculClient.parser.objects;

import calculClient.parser.scene.SceneObjects;
import calculClient.triplet.Color;
import calculClient.triplet.Point;

/**
 * The type Sphere.
 */
public class Sphere extends SceneObjects {
    private final double radius;

    private Point position;

    /**
     * Instantiates a new Sphere.
     *
     * @param position      the position
     * @param radius        the radius
     * @param diffuseColor  the diffuse color
     * @param specularColor the specular color
     * @param shininess     the shininess
     */
    public Sphere(Point position, double radius, Color diffuseColor, Color specularColor, int shininess, Color ambient) {
        super(diffuseColor, specularColor, ambient, shininess);
        this.position = position;
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    /**
     * Gets position.
     *
     * @return the position
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Sets position.
     *
     * @param position the position
     */
    public void setPosition(Point position) {
        this.position = position;
    }
}



