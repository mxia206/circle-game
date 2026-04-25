package game.Projectiles;

import game.*;
/**
 * icicle that slows
 */
public class Icicle extends Projectile {

    public Icicle(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 70.0*Math.cos(dir), 70.0*Math.sin(dir), 70.0, pl);
        this.time = 15;
        this.radius = 5.0;
        this.damage = 5.0;
        this.slow = 0.7;
        this.slow_time = 20;
        this.type = "icicle";
    }

    public void update() {
        this.damage+=1.4;
        super.update();
    }
}
