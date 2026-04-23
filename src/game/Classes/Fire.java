package game.Classes;

import java.util.*;

import game.*;
import game.Projectiles.*;

/**
 * Fire class
 */
public class Fire extends Player {

    public Fire(String id, String name, double x, double y) {
        super(id,name, x, y);
        mana_regen = 2.0;
        health_regen = 0.5;
        this.gameClass = "fire";

        this.skill1maxcd = 2;
        this.skill2maxcd = 5;
        this.skill3maxcd = 100;
    }
    

    //small fireball
    public Set<Projectile> skill_1 (double dir) {
        double manacost = 15.0;
        if (mana <= manacost || skill1cd > 0) {
            return null;
        }
        skill1cd = skill1maxcd;
        mana -= manacost;
        String projectileId = UUID.randomUUID().toString();
        return new HashSet<Projectile> (Arrays.asList(new Fireball(projectileId, this.x, this.y, dir, this)));
    }

    //larger fireball
    public Set<Projectile>  skill_2 (double dir) {
        double manacost = 30.0;
        if (mana <= manacost || skill2cd > 0) {
            return null;
        }
        skill2cd = skill2maxcd;
        mana -= manacost;
        String projectileId = UUID.randomUUID().toString();
        return new HashSet<Projectile> (Arrays.asList(new ChonkyFireball(projectileId, this.x, this.y, dir, this)));
    }

    //big slow cluster fireball
    public Set<Projectile>  skill_3 (double dir) {
        double manacost = 90.0;
        if (mana <= manacost || skill3cd > 0) {
            return null;
        }
        skill3cd = skill3maxcd;
        mana -= manacost;
        String projectileId = UUID.randomUUID().toString();
        return new HashSet<Projectile> (Arrays.asList(new ClusterFireball(projectileId, this.x, this.y, dir, this)));
    }

}
