package calculClient.parser.light;

import calculClient.triplet.Color;
import calculClient.triplet.Point;
import calculClient.triplet.Vector;

/**
 * The type Light.
 *
 * @author damien.allaert
 */
public abstract class Light {

    private Color color;

    /**
     * Instantiates a new Light.
     *
     * @param color      the color
     */
    protected Light(Color color){
        this.color = color;
    }

    /**
     * Gets color.
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets color.
     *
     * @param color the color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    public abstract Vector getLdir(Point p);
}
