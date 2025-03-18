package org.dksd.tasks;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleParser {

    public static void main(String[] args) {
        ScheduleParser scheduleParser = new ScheduleParser();
        scheduleParser.parse("Weekly Mon,Tue,Wed,Thur,Fri before 8:15am");
    }

    class Sched {
        private String entry;
        private Freq frequency;
        private DayOfWeek[] daysOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;

        public Sched(String entry, Freq freq, DayOfWeek[] daysOfWeek, LocalTime startTime, LocalTime endTime) {
            this.entry = entry;
            this.frequency = freq;
            this.daysOfWeek = daysOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public Sched parse(String schedule) {
        //split schedule into lines
        String[] lines = schedule.toLowerCase().split(" ");
        String freq = lines[0];
        String[] days = lines[1].split(",");
        String when = lines[2];
        String time = lines[3];
        //Sched sched = new Sched(schedule, freq, days, );
        //System.out.println(often + lines[1] + when + time);
        //return sched;
        return null;
    }
}
