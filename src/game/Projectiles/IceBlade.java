package game.Projectiles;

import game.*;
/**
 * iceblade lingers/damage over time
 */
public class IceBlade extends Projectile {
    public IceBlade(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 70.0*Math.cos(dir), 70.0*Math.sin(dir), 70.0, pl);
        this.time = 50;
        this.radius = 50.0;
        this.damage = 10.0;
        this.slow = 0.2;
        this.slow_time = 10;
        this.type = "iceblade";
    }

    public void update() {
        if (this.time <= 45) {
            this.x_vel = 0.0; this.y_vel = 0.0;
            if (this.time % 5 == 0) {
                this.hitPlayers.clear();
                this.hitPlayers.add(this.myPlayer.id);
            }
        }
        super.update();
    }
}
