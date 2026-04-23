package game;
import java.util.*;

/**
 * Template for projectiles
 */
public abstract class Projectile extends Circle {
    public int time;
    public double damage;
    public Set<String> hitPlayers;
    public Player myPlayer;
    public String type;

    //slow effect
    public double slow = 1.0;
    public int slow_time = 0;
    public int stun_time = 0;

    /**
     * constructs Projectile
     * @param id id
     * @param x x position
     * @param y y position
     * @param x_vel x velocity
     * @param y_vel y velocity
     * @param vel maximum velocity
     * @param pl owning player
     */
    public Projectile(String id, double x, double y, double x_vel, double y_vel, double vel, Player pl) {
        super(id, x, y);
        hitPlayers = new HashSet<String>();
        myPlayer = pl;
        max_vel = vel;
        hitPlayers.add(pl.id);
        updateVelocity(x_vel, y_vel);
    }

    public void update() {
        super.update();
        time--;
    }
}
