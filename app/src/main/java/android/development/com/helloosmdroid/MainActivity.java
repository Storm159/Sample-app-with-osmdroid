package android.development.com.helloosmdroid;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.development.com.helloosmdroid.databinding.ActivityMainBinding;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;

public class MainActivity extends AppCompatActivity {

    // Private fields
    private ActivityMainBinding mBinding;
    private Context mApplicationContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mApplicationContext = getApplicationContext();

        if (mApplicationContext == null) return;

        mBinding.mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mBinding.mapView.setBuiltInZoomControls(true);
        mBinding.mapView.setMultiTouchControls(true);
        mBinding.mapView.getController().setZoom(18);
        mBinding.mapView.setMaxZoomLevel(18);
        mBinding.mapView.setMinZoomLevel(18);

        GeoPoint geoPoint = new GeoPoint(22.80979, 89.56439);
        mBinding.mapView.getController().animateTo(geoPoint);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(cacheManagingTask);
    }

    private Runnable cacheManagingTask = new Runnable() {
        @Override
        public void run() {

            final SqlTileWriter writer = new SqlTileWriter();

            CacheManager cacheManager = new CacheManager(mBinding.mapView, writer);

            cacheManager.downloadAreaAsync(MainActivity.this, mBinding.mapView.getBoundingBox(),
                    mBinding.mapView.getMinZoomLevel(), mBinding.mapView.getMaxZoomLevel(),
                    new CacheManager.CacheManagerCallback() {
                        @Override
                        public void onTaskComplete() {
                            Log.e("onTaskComplete()", "Download has been completed");

                            if (writer != null) {
                                writer.onDetach();
                            }
                        }

                        @Override
                        public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
                            Log.e("updateProgress()", "Current progress = " + progress + ", current zoom level = " + currentZoomLevel);
                        }

                        @Override
                        public void downloadStarted() {
                            Log.e("downloadStarted()", "Download has been started");
                        }

                        @Override
                        public void setPossibleTilesInArea(int total) {
                            Log.e("possibleTilesInArea()", "There are " + total + " tiles.");
                        }

                        @Override
                        public void onTaskFailed(int errors) {
                            Log.e("onTaskFailed()", "There are " + errors + " errors.");

                            if (writer != null) {
                                writer.onDetach();
                            }
                        }
                    });
        }
    };
}
