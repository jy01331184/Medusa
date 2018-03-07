package medusa.com.bundle3;

import android.content.Intent;
import android.os.Bundle;

import com.bundle2.Bundle2Activity;
import com.medusa.application.MedusaAgent;
import com.medusa.application.MedusaBundle;

/**
 * Created by tianyang on 17/5/8.
 */
public class KDBundle implements MedusaBundle {
    @Override
    public void onCreate() {
        System.out.println("KDBundle oncreate:"+Bundle2Activity.class);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStart(Bundle bundle) {

        Intent intent = new Intent(MedusaAgent.getInstance().getApplication(), Bundle3Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MedusaAgent.getInstance().getApplication().startActivity(intent);
    }
}
