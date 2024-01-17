package sae101;

import sae101.parser.Parser;
import sae101.parser.scene.Scene;
import sae101.raytracer.RayTracer;

import java.io.IOException;
/**
 * The type Main.
 */
public class Main {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) throws IOException {
        Parser pars;
        pars = new Parser(args[0]);
        Scene scene = pars.build();
        RayTracer rt = new RayTracer(scene);
        rt.view();
    }
}