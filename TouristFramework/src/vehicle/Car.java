package vehicle;

import core.Component;
import core.Vehicle;

// 修复点2: 加上 @app.Component 和 public
@Component
public class Car implements Vehicle{
    @Override
    public void drive() {
        System.out.println("开车上路。。。.");
    }
}
