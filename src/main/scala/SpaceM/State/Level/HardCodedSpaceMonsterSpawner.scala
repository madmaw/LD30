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
  var baseJellyRadius = 50
  val explosionSoundEffect = new SoundEffect(Array("explosion1.wav", "explosion2.wav", "explosion3.wav", "explosion4.wav"))

  val asteroidMind = new AsteroidMind(explosionSoundEffect)
  val projectileMind = new ProjectileMind
  val jellyMind = new ProjectileMind

  override def reset(): Unit = {
    debt = 0L;
    credit = -5000L;
  }

  def createBonus(width: Int, level: LevelState): Monster = {
    val x = Math.random() * width
    val y = -powerUpOxygenRadius
    val bonusActivity = new ProjectileActivity(
      0.003,
      Math.PI/2,
      0.04,
      0
    )
    var oxyBonus = (level.maximumOxygen - level.oxygen) / level.maximumOxygen
    oxyBonus = oxyBonus * oxyBonus * oxyBonus
    var powerUpType = MonsterType.PowerUpOxygen
    val powerUpTypeSelector = Math.random()
    if( powerUpTypeSelector < 0.4 + oxyBonus ) {
      powerUpType = MonsterType.PowerUpOxygen
    } else if( powerUpTypeSelector < 0.7 ) {
      powerUpType = MonsterType.PowerUpRapid
    } else if( powerUpTypeSelector < 0.9 ) {
      powerUpType = MonsterType.PowerUpSpread
    } else {
      powerUpType = MonsterType.PowerUpFreeze
    }
    val bonus = new Monster(
      powerUpType,
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
      this.credit -= 14000;
      //this.credit -= 2000;
      val bonus = createBonus(width, level)
      result += bonus
    }
    while( this.debt > 0 ) {
      val selector = Math.random()
      if( level.distance > 1000 && selector > 0.9 ) {
        val jellyToughness = Math.max( 1, Math.random() * level.distance / 1500)
        val jellyRadius = this.baseJellyRadius * jellyToughness
        this.debt -= (100 * jellyRadius * 2).toInt
        val x = Math.random() * width
        val y = -jellyRadius;
        val jelly = new Monster(
          MonsterType.Jelly,
          jellyMind,
          x.toInt,
          y.toInt,
          jellyRadius.toInt,
          -1
        )
        val jellyActivity = new ProjectileActivity(
          0,
          Math.PI/2,
          0.09,
          jellyToughness.toInt
        )
        jelly.setActivity(jellyActivity)
        result += jelly
      } else {
        val asteroidRadius = (this.baseAsteroidRadius * (1 + level.distance/500) * (0.5 + Math.random())).toInt;
        this.debt -= 100 * asteroidRadius
        val asteroidToughness = Math.max(1, asteroidRadius / this.baseAsteroidRadius)
        val x = Math.random() * width
        val y = -asteroidRadius;
        var secretBonus: Monster = null
        if( Math.random() > 0.93 ) {
          secretBonus = createBonus(width, level)
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
    }
    return result;
  }

}
