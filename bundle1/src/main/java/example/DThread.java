package example;

/**
 * Created by tianyang on 16/9/28.
 */
public class DThread extends Thread {

    @Override
    public void run() {

        new A().a();

    }
}
