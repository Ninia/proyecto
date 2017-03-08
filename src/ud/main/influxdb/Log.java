package ud.main.influxdb;


import org.w3c.dom.Document;
import ud.main.influxdb.monitor.CPU;
import ud.main.influxdb.monitor.Memory;
import ud.main.utils.DocumentReader;

import java.util.ArrayList;

public class Log {

    protected static Document document = DocumentReader.getDoc("config/System.xml");
    protected static String SYSNAME = Log.document.getElementsByTagName("hostname").item(0).getTextContent();

    @SuppressWarnings("unchecked")
    public static Point getCPUUsagePoint(){

        Point point = new Point();
        point.setMeasurement("cpu_usage");
        point.getTags().put("host", "\"" + SYSNAME + "\"");
        point.getFields().put("value", CPU.getUsage());
        point.setTime(System.currentTimeMillis());

        return point;
    }


    @SuppressWarnings("unchecked")
    public static Point getCPUTemperaturePoint(){

        Point point = new Point();
        point.setMeasurement("cpu_temperature");
        point.getTags().put("host", "\"" + SYSNAME + "\"");
        point.getFields().put("value", CPU.getTemperature());
        point.setTime(System.currentTimeMillis());

        return point;
    }

    @SuppressWarnings("unchecked")
    public static Point getMemUsagePoint(){
        Point point = new Point();
        point.setMeasurement("mem_usage");
        point.getTags().put("host", "\"" + SYSNAME + "\"");
        point.getFields().put("value", Memory.getUsage());
        point.setTime(System.currentTimeMillis());

        return point;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Point> generatePoints(int n,int s) {
        ArrayList<Point> points = new ArrayList<>();
        for (int i=0; i<n; i++){
            points.add(getCPUUsagePoint());
            points.add(getCPUTemperaturePoint());
            points.add(getMemUsagePoint());
            try {
                Thread.sleep(s * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return points;
    }


    public static void main(String[] args) {
        String db = "server_stats";
        InfluxDB.createDataBase(db);
        ArrayList<Point> points = generatePoints(3, 0);
        for (Point point: points) {
            System.out.println(point);
        }
        InfluxDB.writePoints(db, points);
    }
}