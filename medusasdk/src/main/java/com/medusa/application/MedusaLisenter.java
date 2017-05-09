package com.medusa.application;

/**
 * Created by tianyang on 17/5/8.
 */
public interface MedusaLisenter {

    void onMedusaLoad(MedusaLoadState medusaLoadState);

    void onBundleLoad(String name,boolean success);

    public static class MedusaLoadState{

        public float progress ;

        public boolean success = true;

        public MedusaLoadState(boolean success) {
            this.success = success;
        }

        public MedusaLoadState(float progress) {
            this.progress = progress;
        }

        public static final MedusaLoadState FAIL = new MedusaLoadState(false);
    }

    public MedusaLisenter NULL = new MedusaLisenter() {
        @Override
        public void onMedusaLoad(MedusaLoadState medusaLoadState) {

        }

        @Override
        public void onBundleLoad(String name,boolean success) {

        }
    };

}
