package accommodation;

import core.Accommodation;
import core.Component;

@Component
public class Inn implements Accommodation{
    @Override
    public void checkIn(int count) {
        System.out.println(count+"个人入住一家酒店。。。");
    }
}
