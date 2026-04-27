package game.Projectiles;

/**
 * small fireball
 */
import game.*;

public class Fireball extends Projectile {

    public Fireball(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 100.0*Math.cos(dir), 100.0*Math.sin(dir), 100.0, pl);
        this.time = 12;
        this.radius = 8.0;
        this.damage = 25.0;
        this.type = "fireball";
    }

}
