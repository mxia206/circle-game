package game.Projectiles;

import game.*;
/**
 * shockwave that stuns
 */
public class Shockwave extends Projectile {
    public Shockwave(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 0, 0, 0, pl);
        this.time = 4;
        this.radius = 100.0;
        this.damage = 10.0;
        this.stun_time = 10;
        this.slow = 0.7;
        this.slow_time = 20;
        this.type = "shockwave";
    }
}
