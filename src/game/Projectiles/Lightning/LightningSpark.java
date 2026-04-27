package game.Projectiles;

import game.Player;

import game.*;

public class LightningSpark extends Projectile {
    public LightningSpark(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 60.0*Math.cos(dir), 60.0*Math.sin(dir), 60.0, pl);
        this.time = 5;
        this.radius = 5;
        this.damage = 10.0;
        this.stun_time = 3;
        this.type = "lightningspark";
    }
}
