package ud.binmonkey.prog3_proyecto_server.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static String currentFormattedDate() {
        return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")).format(new Date());
    }

    public static void main(String[] args) {
        System.out.println(currentFormattedDate());
    }
}
