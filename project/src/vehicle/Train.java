package vehicle;

import core.Component;
import core.Vehicle;

// 修复点4: 加上 @app.Component 和 public
@Component
public class Train implements Vehicle{
    @Override
    public void drive() {
        System.out.println("火车开动了。。。");
    }
}
