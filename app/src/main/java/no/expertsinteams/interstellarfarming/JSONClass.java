package no.expertsinteams.interstellarfarming;

/**
 * Created by Anders on 13.04.2016.
 */
public class JSONClass {
    public String name;
    public String status;
    public float[] area;
    public float[] waypoints_x;
    public float[] waypoints_y;

    public JSONClass(String name, String status, float[] area, float[] waypoints_x, float[] waypoints_y) {
        this.name = name;
        this.status = status;
        this.area = area;
        this.waypoints_x = waypoints_x;
        this.waypoints_y = waypoints_y;
    }
}
