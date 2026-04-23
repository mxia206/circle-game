package game.Classes;

import java.util.*;

import game.*;
import game.Projectiles.*;

/**
 * Ice class
 */
public class Ice extends Player {

    public Ice(String id, String name, double x, double y) {
        super(id,name, x, y);
        mana_regen = 2.0;
        health_regen = 0.5;
        this.gameClass = "ice";

        this.skill1maxcd = 10;
        this.skill2maxcd = 50;
        this.skill3maxcd = 70;
    }

    //triple icicle shot
    public Set<Projectile> skill_1 (double dir) {
        double manacost = 20.0;
        if (mana <= manacost || skill1cd > 0) {
            return null;
        }
        skill1cd = skill1maxcd;
        mana -= manacost;
        Set<Projectile> res = new HashSet<Projectile> ();
        for (int i = -1; i <= 1; i++) {
            res.add(new Icicle(UUID.randomUUID().toString(),x,y,dir+i*Math.PI/12, this)); 
        }
        return res;
    }

    //spinning blade
    public Set<Projectile>  skill_2 (double dir) {
        double manacost = 20.0;
        if (mana <= manacost || skill2cd > 0) {
            return null;
        }
        skill2cd = skill2maxcd;
        mana -= manacost;
        String projectileId = UUID.randomUUID().toString();
        return new HashSet<Projectile> (Arrays.asList(new IceBlade(projectileId, this.x, this.y, dir, this)));
    }

    //snowstorm
    public Set<Projectile>  skill_3 (double dir) {
        double manacost = 50.0;
        if (mana <= manacost || skill3cd > 0) {
            return null;
        }
        skill3cd = skill3maxcd;
        mana -= manacost;
        String projectileId = UUID.randomUUID().toString();
        return new HashSet<Projectile> (Arrays.asList(new SnowStorm(projectileId, this.x, this.y, dir, this)));
    }

}