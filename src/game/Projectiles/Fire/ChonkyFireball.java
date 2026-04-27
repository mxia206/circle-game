package game.Projectiles;

import game.*;
/**
 * larger fireball
 */
public class ChonkyFireball extends Projectile {

    public ChonkyFireball(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 70.0*Math.cos(dir), 70.0*Math.sin(dir), 70.0, pl);
        this.time = 18;
        this.radius = 20.0;
        this.damage = 50.0;
        this.type = "chonkyfireball";
    }

}
