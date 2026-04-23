package game.Projectiles;

import game.*;
/**
 * bloodblade
 */
public class BloodBlade extends Projectile {

    public BloodBlade(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 60.0*Math.cos(dir), 60.0*Math.sin(dir), 60.0, pl);
        this.time = 5;
        this.radius = 5.0;
        this.damage = 5.0;
        this.type = "bloodblade";
    }

}
