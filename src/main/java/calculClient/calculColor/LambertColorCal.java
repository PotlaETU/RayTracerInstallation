package calculClient.calculColor;

import calculClient.parser.light.Light;
import calculClient.parser.objects.Sphere;
import calculClient.parser.scene.Scene;
import calculClient.triplet.Color;
import calculClient.triplet.Point;
import calculClient.triplet.Triplet;
import calculClient.triplet.Vector;

public class LambertColorCal implements IFormLambert{
    @Override
    public Color calculateColor(Sphere sphere, Scene scene, Point p) {
        Vector n = sphere.getPosition().sub(p).normalize();
        Color col = new Color(0,0,0);

        for(Light light : scene.getLight()){
            Vector ldir = light.getLdir(p);
            double cosTheta = Math.max(n.scalarProduct(ldir),0);
            col = col.add(light.getColor().multiply(cosTheta));
        }
        if(sphere.getDiffuseColor()!=null){
            Triplet colCoor = col.schurProduct(sphere.getDiffuseColor()).getCoor();
            colCoor.setX(Math.min(colCoor.getX(),1));
            colCoor.setY(Math.min(colCoor.getY(),1));
            colCoor.setZ(Math.min(colCoor.getZ(),1));
            col.setCoor(colCoor);
        }
        if(scene.getAmbient()!=null){
            col = col.add(scene.getAmbient());
        }
        if(sphere.getAmbient()!=null){
            Triplet colCoor = col.add(sphere.getAmbient()).getCoor();
            colCoor.setX(Math.min(colCoor.getX(),1));
            colCoor.setY(Math.min(colCoor.getY(),1));
            colCoor.setZ(Math.min(colCoor.getZ(),1));
            col.setCoor(colCoor);
        }
        return col;
    }
}
