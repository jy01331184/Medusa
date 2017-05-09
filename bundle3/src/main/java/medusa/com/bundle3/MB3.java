package medusa.com.bundle3;

import android.os.Bundle;

import com.medusa.application.MedusaBundle;

/**
 * Created by tianyang on 17/5/8.
 */
public class MB3 implements MedusaBundle {
    @Override
    public void onCreate() {
        System.out.println("MB3 oncreate");
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStart(Bundle bundle) {
        System.out.println("MB3 onStart");
    }
}
