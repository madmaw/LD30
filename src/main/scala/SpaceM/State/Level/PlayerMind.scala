package SpaceM.State.Level

import javax.sound.sampled.Clip

import SpaceM.Driver.Input
import SpaceM.SoundEffect

/**
 * Created by chris on 23/08/14.
 */
class PlayerMind(
  var input: Input,
  val damageSoundEffect: SoundEffect,
  val fireSoundEffect: SoundEffect,
  val powerUpSoundEffect: SoundEffect
) extends Mind {

  var weapon: Weapon = new PeaShooterWeapon(1000)
  var thrustMind = new ProjectileMind()


  override def think(monster: Monster, delta: Long, level: LevelState, width: Int, height: Int): Boolean = {

    val dx = monster.positionX - this.input.positionX
    val dy = monster.positionY - this.input.positionY
    val angle = Math.atan2(dy, dx)

    val d = (Math.sqrt(dx*dx + dy*dy) * delta) / 2000

    var progress = d
    while( progress > 0 ) {
      progress -= 1
      val ddx = ((progress / d) * dx).toInt
      val ddy = ((progress / d) * dy).toInt
      val thrust = new Monster(
        MonsterType.Thrust,
        thrustMind,
        monster.positionX + ddx,
        monster.positionY + ddy,
        0,
        -1
      )
      thrust.activity = new ProjectileActivity(
        0.005 - Math.random() * 0.01,
        angle,
        0.1 + Math.random() * 0.1,
        1
      )
      level.particles += thrust
    }

    monster.positionX = this.input.positionX
    monster.positionY = this.input.positionY

    if( this.weapon != null ) {
      val angle = input.personalizationImageAngle - Math.PI/2
      val sin = Math.sin(angle)
      val cos = Math.cos(angle)
      val dx = cos * monster.radius
      val dy = sin * monster.radius

      val bullets = this.weapon.fire(delta, monster.positionX + dx.toInt, monster.positionY + dy.toInt, angle)
      if( bullets != null && bullets.length > 0 ) {
        fireSoundEffect.play(monster.playerInputId)
        for( bullet <- bullets ) {
          level.friendlyBullets += bullet
        }
      }
    }



    return false;
  }

  override def collision(monster: Monster, collidedWith: Monster, level: LevelState): Boolean = {
    if( collidedWith.monsterType == MonsterType.Asteroid ) {
      val activity = collidedWith.activity
      activity match {
        case projectileActivity: ProjectileActivity =>
          level.oxygen -= 5 * projectileActivity.toughness
          damageSoundEffect.play()
      }
    } else if( collidedWith.monsterType == MonsterType.PowerUpOxygen ) {
      level.oxygen = Math.min(level.oxygen + 40, level.maximumOxygen)
      powerUpSoundEffect.play()
    }
    return false;
  }

}
