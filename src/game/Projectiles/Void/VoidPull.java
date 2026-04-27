package game.Projectiles;

import java.util.Collection;

import game.*;


public class VoidPull extends Projectile {
    public VoidPull(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 40.0*Math.cos(-dir), 40.0*Math.sin(-dir), 40.0, pl);
        this.time = 30;
        this.radius = 20.0;
        this.stun_time = 10;
        this.damage = 0;
        this.type = "voidpull";
        this.multhit = true;
    }

    public void update() {
        if (myPlayer.collision(this)) {
            this.time = 0;
        }
        super.update();
    }

    public void computeHoming() {
        double targetdir = Math.atan2(myPlayer.y-this.y, myPlayer.x-this.x);
        this.x_vel = (max_vel*Math.cos(targetdir));
        this.y_vel = (max_vel*Math.sin(targetdir));
    }

}
