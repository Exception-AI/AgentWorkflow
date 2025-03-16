package org.dksd.tasks;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduleParser {

    public static void main(String[] args) {
        ScheduleParser scheduleParser = new ScheduleParser();
        scheduleParser.parse("Every Mon,Tue,Wed,Thur,Fri before 8:15am");
    }

    class Sched {
        private DayOfWeek[] daysOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    public Sched parse(String schedule) {
        //split schedule into lines
        String[] lines = schedule.toLowerCase().split(" ");
        String often = lines[0];
        String[] days = lines[1].split(",");
        String when = lines[2];
        String time = lines[3];
        Sched sched = new Sched();
        System.out.println(often + lines[1] + when + time);
        return sched;
    }
}
