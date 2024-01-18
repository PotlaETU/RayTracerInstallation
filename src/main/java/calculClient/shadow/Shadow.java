package calculClient.shadow;

import calculClient.parser.Parser;
import calculClient.parser.scene.SceneBuilder;

public class Shadow implements IShadow{

    private SceneBuilder sceneBuilder = Parser.getSceneBuilder();
    @Override
    public boolean request() {
       return sceneBuilder.getShadow();
    }
}
