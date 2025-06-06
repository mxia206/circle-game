package tumtumtumsahur.Projectiles;

import tumtumtumsahur.*;
/**
 * cluster fireball that bursts on impact
 */
public class ClusterFireball extends Projectile{

    public ClusterFireball(String id, double x_pos, double y_pos, double dir, Player pl) {
        super(id, x_pos, y_pos, 40.0*Math.cos(dir), 40.0*Math.sin(dir), 40.0, pl);
        this.time = 8;
        this.radius = 30.0;
        this.damage = 50.0;
        this.stun_time = 20;
        this.type = "clusterfireball";
    }
}
