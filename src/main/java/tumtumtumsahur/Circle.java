package tumtumtumsahur;

/**
 * Basic circle class to handle updates and collisions
 */
public class Circle {    

    public String id;
    double last_x;
    double last_y;
    public double x;
    public double y;
    public double x_vel;
    public double y_vel;
    public double max_vel;
    public double radius;
    

    /**
     * Constructs a circle with id and position
     * @param id id
     * @param x x position
     * @param y y position
     */
    Circle(String id, double x, double y) {
        this.id = id;
        this.last_x = x;
        this.last_y = y;
        this.x = x;
        this.y = y;
        this.x_vel = 0;
        this.y_vel = 0;
    }
    /**
     * Checks collision between two circles
     * @param ref other circle to reference collision
     * @return
     */
    public boolean collision (Circle ref) {
        int stepcount = 25;
        double stepx = (ref.x-ref.last_x)/stepcount;
        double stepy = (ref.y-ref.last_y)/stepcount;
        for (int i = 1; i <= stepcount; i++) {
            if (Math.hypot(ref.last_x+stepx*i-x,ref.last_y+stepy*i-y) < radius+ref.radius) {
                return true;
            }
        }
        return false;
    }
    /**
     * updates velocity 
     * @param x x velocity
     * @param y y velocity
     */
    public void updateVelocity (double x, double y) {
        double magnitude = Math.hypot(x, y);
        if (magnitude != 0) {
            x /= magnitude; y /= magnitude;
        }
        x_vel = x*max_vel; y_vel = y*max_vel;
    }
    /**
     * updates current position
     */
    public void update() {
        last_x = x;
        last_y = y;
        x += x_vel;
        y += y_vel;
    }
}