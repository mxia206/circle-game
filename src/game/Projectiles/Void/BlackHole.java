package game.Projectiles;

import game.*;
/**
 * snowstorm that slows over area
 */
public class BlackHole extends Projectile {
    public BlackHole(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 0, 0, 0, pl);
        this.time = 50;
        this.radius = 250.0;
        this.damage = 0.6;
        this.slow = 0.35;
        this.slow_time = 10;
        this.type = "blackhole";
        this.multhit = true;
    }
}
