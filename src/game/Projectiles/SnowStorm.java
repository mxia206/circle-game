package game.Projectiles;

import game.*;
/**
 * snowstorm that slows over area
 */
public class SnowStorm extends Projectile {
    public SnowStorm(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 0, 0, 0, pl);
        this.time = 50;
        this.radius = 150.0;
        this.damage = 15.0;
        this.slow = 0.8;
        this.slow_time = 10;
        this.type = "snowstorm";
    }

    public void update() {
        if (this.time % 10 == 0) {
            this.hitPlayers.clear();
            this.hitPlayers.add(this.myPlayer.id);
        }
        super.update();
    }
}
