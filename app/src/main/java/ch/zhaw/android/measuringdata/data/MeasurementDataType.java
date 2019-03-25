package ch.zhaw.android.measuringdata.data;

public class MeasurementDataType {
    public static int MAX = 10; //Hard Coded Max Measurements to save

    String      timestamp;
    String      measurement;



    public String getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    @Override
    public String toString() {
        return "Measurement{ " +
                " ,timestamp=" + timestamp + "   " +
                " ,measurement='" + measurement + '\'' +
                '}';
    }
}
