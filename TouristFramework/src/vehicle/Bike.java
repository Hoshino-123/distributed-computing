package vehicle;

import core.Component;
import core.Vehicle;

// 修复点1: 加上 @app.Component 和 public
@Component
public class Bike implements Vehicle{
    @Override
    public void drive() {
        System.out.println("骑自行车上路。。。.");
    }
}
