package medusa.com.bundle3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by tianyang on 16/8/17.
 */
public class Bundle3Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("receive in bundle3");
    }
}
