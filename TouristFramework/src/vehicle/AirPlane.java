package vehicle;

import core.Component;
import core.Vehicle;

// 修复点3: 加上 @app.Component 和 public
@Component
public class AirPlane implements Vehicle{
    @Override
    public void drive() {
        System.out.println("飞机起飞。。。");
    }
}
