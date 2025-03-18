package org.dksd.tasks.scheduling;

public class ScheduleParser {

    public static void main(String[] args) {
        ScheduleParser scheduleParser = new ScheduleParser();
        scheduleParser.parse("Weekly Mon,Tue,Wed,Thur,Fri before 8:15am");
    }

    public void parse(String schedule) {
        //split schedule into lines
        String[] lines = schedule.toLowerCase().split(" ");
        String freq = lines[0];
        String[] days = lines[1].split(",");
        String when = lines[2];
        String time = lines[3];
        //Sched sched = new Sched(schedule, freq, days, );
        //System.out.println(often + lines[1] + when + time);
        //return sched;
    }
}
