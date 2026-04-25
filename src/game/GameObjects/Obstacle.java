package game;

/**
 * In game obstacles
 */
public class Obstacle extends Circle {
    /**
     * constructs new obstacle
     * @param id id
     * @param x x position
     * @param y y position
     * @param radius circle radius
     */
    public Obstacle (String id, double x, double y, double radius) {
        super(id, x, y);
        this.radius = radius;
    }

}
