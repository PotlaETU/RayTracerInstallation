package calculClient.shadow;

public class Proxy implements IShadow{

    private Shadow shadow;

    public Proxy(){
        this.shadow = new Shadow();
    }

    @Override
    public boolean request() {
        return shadow.request();
    }
}
