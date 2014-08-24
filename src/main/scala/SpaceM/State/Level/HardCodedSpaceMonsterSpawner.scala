package SpaceM.State.Level

import SpaceM.SoundEffect

import scala.collection.mutable.ArrayBuffer

/**
 * Created by chris on 23/08/14.
 */
class HardCodedSpaceMonsterSpawner(
  val powerUpOxygenRadius: Int
) extends MonsterSpawner {

  var debt = 0L;
  var credit = 0D;
  var baseAsteroidRadius = 35
  val explosionSoundEffect = new SoundEffect(Array("explosion1.wav", "explosion2.wav", "explosion3.wav", "explosion4.wav"))

  val asteroidMind = new AsteroidMind(explosionSoundEffect)
  val projectileMind = new ProjectileMind

  override def reset(): Unit = {
    debt = 0L;
    credit = -5000L;
  }

  def createBonus(width: Int): Monster = {
    val x = Math.random() * width
    val y = -powerUpOxygenRadius
    val bonusActivity = new ProjectileActivity(
      0.003,
      Math.PI/2,
      0.04,
      0
    )
    val bonus = new Monster(
      MonsterType.PowerUpOxygen,
      projectileMind,
      x.toInt,
      y.toInt,
      powerUpOxygenRadius,
      -1
    )
    bonus.activity = bonusActivity
    return bonus
  }

  override def accumulate(delta: Long, level: LevelState, width: Int, height: Int): ArrayBuffer[Monster] = {
    this.debt += (delta + level.distance.toLong)
    this.credit += delta * Math.random()
    val result = new ArrayBuffer[Monster]()
    while( this.credit > 0 ) {
      this.credit -= 20000;
      val bonus = createBonus(width)
      result += bonus
    }
    while( this.debt > 0 ) {
      val asteroidRadius = (this.baseAsteroidRadius * (1 + level.distance/500) * (0.5 + Math.random())).toInt;
      this.debt -= 100 * asteroidRadius
      val asteroidToughness = Math.max(1, asteroidRadius / this.baseAsteroidRadius)
      val x = Math.random() * width
      val y = -asteroidRadius;
      var secretBonus: Monster = null
      if( Math.random() > 0.95 ) {
        secretBonus = createBonus(width)
        this.credit -= 1000;
      }
      val asteroidActivity = new AsteroidActivity(
        Math.random() * Math.PI / 1000,
        Math.PI/4 + Math.PI/2 * Math.random(),
        0.05 + Math.random()* 0.1,
        asteroidToughness,
        null,
        secretBonus
      );
      val asteroid = new Monster(
        MonsterType.Asteroid,
        asteroidMind,
        x.toInt,
        y.toInt,
        asteroidRadius,
        -1
      )
      asteroid.activity = asteroidActivity
      result += asteroid;
    }
    return result;
  }

}
