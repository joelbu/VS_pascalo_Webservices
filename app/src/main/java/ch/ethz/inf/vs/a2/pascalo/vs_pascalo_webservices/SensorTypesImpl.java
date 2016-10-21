package ch.ethz.inf.vs.a2.pascalo.vs_pascalo_webservices;

import android.hardware.Sensor;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SensorTypesImpl {
    private static final String TAG = "SensorTypesImpl";

    public static String getNameFromType(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_TEMPERATURE:
            case 65536: // This is termperature on my Nexus 5X
                return "temperature";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "ambienttemperature";
            case Sensor.TYPE_LIGHT:
                return "light";
            case Sensor.TYPE_PRESSURE:
                return "pressure";
            case Sensor.TYPE_PROXIMITY:
                return "proximity";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "humidity";
            case Sensor.TYPE_ACCELEROMETER:
                return "accelerometer";
            case Sensor.TYPE_GRAVITY:
                return "gravity";
            case Sensor.TYPE_GYROSCOPE:
                return "gyrosope";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "linearacceleration";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "magnetic";
            case Sensor.TYPE_ORIENTATION:
                return "orientation";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "rotation";
            default:
                Log.d(TAG, "Unexpected Sensor");
                return "nosensor";
        }
    }

    private static final Map<String, Integer> nameTypeMap;
    static {
        Map<String, Integer> aMap = new HashMap<>(15);
        aMap.put("temperature", Sensor.TYPE_TEMPERATURE);
        aMap.put("ambienttemperature", Sensor.TYPE_AMBIENT_TEMPERATURE);
        aMap.put("light", Sensor.TYPE_LIGHT);
        aMap.put("pressure", Sensor.TYPE_PRESSURE);
        aMap.put("proximity", Sensor.TYPE_PROXIMITY);
        aMap.put("humidity", Sensor.TYPE_RELATIVE_HUMIDITY);
        aMap.put("accelerometer", Sensor.TYPE_ACCELEROMETER);
        aMap.put("gravity", Sensor.TYPE_GRAVITY);
        aMap.put("gyrosope", Sensor.TYPE_GYROSCOPE);
        aMap.put("linearacceleration", Sensor.TYPE_LINEAR_ACCELERATION);
        aMap.put("magnetic", Sensor.TYPE_MAGNETIC_FIELD);
        aMap.put("orientation", Sensor.TYPE_ORIENTATION);
        aMap.put("rotation", Sensor.TYPE_ROTATION_VECTOR);
        nameTypeMap = Collections.unmodifiableMap(aMap);
    }

    public static int getTypeFromName (String name) {
        return nameTypeMap.get(name);
    }

    public static int getNumberValues(int sensorType){
        switch (sensorType) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_LIGHT:
            case Sensor.TYPE_PRESSURE:
            case Sensor.TYPE_PROXIMITY:
            case Sensor.TYPE_RELATIVE_HUMIDITY:
            case Sensor.TYPE_TEMPERATURE:
            case 65536:
                return 1;
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_ORIENTATION:
            case Sensor.TYPE_ROTATION_VECTOR:
                return 3;
            default:
                return 3;
        }
    }

    public static String getUnitString(String sensorName){
        return getUnitString(getTypeFromName(sensorName));
    }

    public static String getUnitString(int sensorType){

        switch (sensorType) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "°C";
            case Sensor.TYPE_LIGHT:
                return "lx";
            case Sensor.TYPE_PRESSURE:
                return "hPa";
            case Sensor.TYPE_PROXIMITY:
                return "cm";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "%";
            case Sensor.TYPE_TEMPERATURE:
            case 65536:
                return "°C";
            case Sensor.TYPE_ACCELEROMETER:
                return "m/s^2";
            case Sensor.TYPE_GRAVITY:
                return "m/s^2";
            case Sensor.TYPE_GYROSCOPE:
                return "rad/s";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "m/s^2";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "µT";
            case Sensor.TYPE_ORIENTATION:
                return "°";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "";
            default:
                return "";
        }
    }
}
