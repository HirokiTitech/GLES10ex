package jp.ac.titech.itpro.sdl.gles10ex;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SubMenu;
import android.widget.SeekBar;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,SensorEventListener {
    private final static String TAG = "MainActivity";

    private GLSurfaceView glView;
    private SimpleRenderer renderer;
    private SeekBar rotationBarX, rotationBarY, rotationBarZ;

    private SensorManager sensorMgr;
    private Sensor accelerometer;

    private float ax, ay, az;
    private float[] roll,pitch,yaw;
    private float rollAve, pitchAve, yawAve;
    private float rate;
    private int accuracy;
    private long prevts;

    private final static int SUM_NUMBER = 10;
    private static int index;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        glView = (GLSurfaceView) findViewById(R.id.glview);

        rotationBarX = (SeekBar) findViewById(R.id.rotation_bar_x);
        rotationBarY = (SeekBar) findViewById(R.id.rotation_bar_y);
        rotationBarZ = (SeekBar) findViewById(R.id.rotation_bar_z);
        rotationBarX.setOnSeekBarChangeListener(this);
        rotationBarY.setOnSeekBarChangeListener(this);
        rotationBarZ.setOnSeekBarChangeListener(this);

        renderer = new SimpleRenderer();
        renderer.addObj(new Cube(0.5f, 0, 0.2f, -3));
        renderer.addObj(new Pyramid(0.5f, 0, 0, 0));
        glView.setRenderer(renderer);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Toast.makeText(this, getString(R.string.toast_no_accel_error),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        index = 0;
        roll = new float[SUM_NUMBER];
        pitch = new float[SUM_NUMBER];
        yaw = new float[SUM_NUMBER];
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        glView.onResume();
        sensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        glView.onPause();
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        /*
        if (seekBar == rotationBarX)
            renderer.setRotationX(progress);
        else if (seekBar == rotationBarY)
            renderer.setRotationY(progress);
        else if (seekBar == rotationBarZ)
            renderer.setRotationZ(progress);
            */
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ax = event.values[0];
        ay = event.values[1];
        az = event.values[2];
        Log.d(TAG, Float.toString(ax));
        Log.d(TAG, Float.toString(ay));
        Log.d(TAG, Float.toString(az));
        rate = ((float) (event.timestamp - prevts)) / (1000 * 1000);
        prevts = event.timestamp;

        roll[index] = 0;
        pitch[index] = (float) Math.atan(ax/Math.sqrt(ay * ay + az * az));
        yaw[index] = (float) Math.atan(ay/az);

        rollAve = 0;
        pitchAve = 0;
        yawAve = 0;
        for(int i = 0; i < SUM_NUMBER;i++) {
            rollAve += roll[i] / SUM_NUMBER;
            pitchAve += pitch[i] / SUM_NUMBER;
            yawAve += yaw[i] / SUM_NUMBER;
        }

        renderer.setRotationZ((float) (rollAve * 180 / Math.PI));
        renderer.setRotationY((float) (pitchAve * 180 / Math.PI));
        renderer.setRotationX((float) (yawAve * 180 / Math.PI));

        index++;
        if(index >= SUM_NUMBER) {
            index = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "onAccuracyChanged: ");
        this.accuracy = accuracy;
    }
}
