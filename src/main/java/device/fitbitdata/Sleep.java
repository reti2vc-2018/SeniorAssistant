package device.fitbitdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sleep {

    private int minutesAsleep;

    private List<SleepData> datas;

    public int getMinutesAsleep() {
        return minutesAsleep;
    }

    public List<SleepData> getDatas() {
        return datas;
    }

    @JsonProperty("summary")
    public void setMinutesAsleep(Map<String, Object> map) {
        minutesAsleep = (int) map.get("totalMinutesAsleep");
    }

    @JsonProperty("sleep")
    public void setSleepsList(Map<String, Object>[] array) {
        datas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat();
        for(Map<String, Object> map : array) {
            Date date_start = null;
            try {
                date_start = sdf.parse((String) map.get("startTime"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int duration = (int) map.get("duration");

            datas.add(new SleepData(date_start, duration));
        }
    }

    public class SleepData {
        public final Timestamp start_date;
        public final long duration;
        public final Timestamp end_date;

        public SleepData(Date start_date, long duration) {
            this.start_date = new Timestamp(start_date.getTime());
            this.duration = duration;
            this.end_date = start_date!=null? new Timestamp(start_date.getTime() + duration):null;
        }
    }
}
