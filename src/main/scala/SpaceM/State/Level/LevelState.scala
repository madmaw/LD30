package SpaceM.State.Level

import java.awt.{Color, Graphics2D}
import java.util

import SpaceM.Driver.Driver
import SpaceM.SoundEffect
import SpaceM.State.State

import scala.collection.mutable.ArrayBuffer

/**
 * Created by chris on 23/08/14.
 */
class LevelState(
  val driver: Driver,
  val enemySpawner: MonsterSpawner,
  val monsterRendererFactory: MonsterRendererFactory,
  val backgroundColor: Color,
  val playerRadius: Int,
  val maximumOxygen: Double,
  val oxygenUsePerMilli: Double,
  val distancePerMilli: Double
)extends State {

  val players: ArrayBuffer[Monster] = new ArrayBuffer[Monster]()
  val friendlyBullets: ArrayBuffer[Monster] = new ArrayBuffer[Monster]()
  val enemies: ArrayBuffer[Monster] = new ArrayBuffer[Monster]()
  val powerUps: ArrayBuffer[Monster] = new ArrayBuffer[Monster]()
  val particles: ArrayBuffer[Monster] = new ArrayBuffer[Monster]()
  var oxygen: Double = 0
  var paused: Boolean = true;
  var gameOverState: State = null;
  var distance: Double = 0

  val damageSoundEffect = new SoundEffect(Array("damage1.wav","damage2.wav", "damage3.wav", "damage4.wav"))
  val fireSoundEffect = new SoundEffect(Array("shoot3.wav", "shoot2.wav", "shoot1.wav", "shoot4.wav"))
  val powerUpSoundEffect = new SoundEffect(Array("powerup1.wav"))


  override def init(): Unit = {
    // check players
    this.paused = this.checkPlayers(0L)
    this.enemySpawner.reset();
    this.oxygen = maximumOxygen
    this.distance = 0
    this.friendlyBullets.clear()
    this.enemies.clear()
    this.powerUps.clear()
    this.particles.clear()
  }

  def checkPlayers(delta: Long): Boolean = {
    val inputs = this.driver.calculateInputs(delta);
    // remove any players without inputs
    var index = players.length
    while( index > 0 ) {
      index -= 1
      val player = players(index)
      if( !inputs.contains(player.playerInputId) ) {
        players.remove(index)
      }
    }
    for( (id, input) <- inputs ) {
      // find a corresponding player, or create one
      var found = false;
      for( player: Monster <- players ) {
        if( player.playerInputId == id ) {
          found = true;
          val mind = player.mind
          mind match  {
            case playerMind: PlayerMind => playerMind.input = input
          }
        }
      }
      if( !found ) {
        // add it
        val mind = new PlayerMind(input, damageSoundEffect, fireSoundEffect, powerUpSoundEffect)
        val player = new Monster(
          MonsterType.Player,
          mind,
          input.positionX,
          input.positionY,
          playerRadius,
          id
        )
        // TODO set the player as being invulnerable for a period
        players += (player)
      }
    }
    return inputs.size > 0;
  }

  override def update(age: Long, delta: Long, width: Int, height: Int): State = {
    this.paused = !this.checkPlayers(delta)
    if( !this.paused ) {
      val newMonsters = enemySpawner.accumulate(delta, this, width, height)
      if( newMonsters != null ) {
        for( monster <- newMonsters ) {
          if( monster.monsterType == MonsterType.PowerUpOxygen ) {
            this.powerUps += monster;
          } else {
            this.enemies += monster;
          }
        }
      }
      oxygen -= delta * oxygenUsePerMilli;
      // players can't move without oxygen!!
      if( oxygen > 0 ) {
        this.update(delta, this.players, width, height)
      }
      this.update(delta, this.friendlyBullets, width, height)
      this.update(delta, this.enemies, width, height)
      this.update(delta, this.powerUps, width, height)
      this.update(delta, this.particles, width, height)

      distance += distancePerMilli * delta

      // only check certain collisions
      this.checkCollisions(this.players, this.enemies)
      this.checkCollisions(this.players, this.powerUps)
      this.checkCollisions(this.friendlyBullets, this.enemies)

    } else {
      // paused!!!
    }
    if( oxygen >= 0 ) {

      return this
    } else {
      return this.gameOverState
    }
  }

  def update(delta: Long, monsters: ArrayBuffer[Monster], width: Int, height: Int): Unit = {
    var index = monsters.length
    while( index > 0 ) {
      index -= 1
      val monster = monsters(index)
      val mind = monster.mind
      val dead = mind.think(monster, delta, this, width, height)
      if( dead ) {
        monsters.remove(index)
      }
    }
  }

  def checkCollisions(monsters1: ArrayBuffer[Monster], monsters2: ArrayBuffer[Monster]): Unit = {
    var index1 = monsters1.length
    while( index1 > 0 ) {
      index1 -= 1;
      val monster1 = monsters1(index1)
      var monster1Dead = false
      var index2 = monsters2.size;
      while( index2 > 0 && !monster1Dead ) {
        index2 -= 1;
        val monster2 = monsters2(index2)

        val dx = monster1.positionX - monster2.positionX
        val dy = monster1.positionY - monster2.positionY
        val r = monster1.radius + monster2.radius
        if( dx * dx + dy * dy < r * r ) {
          // collision
          monster1Dead = monster1.mind.collision(monster1, monster2, this)
          val monster2Dead = monster2.mind.collision(monster2, monster1, this)
          if( monster2Dead ) {
            monsters2.remove(index2)
          }
        }
      }
      if( monster1Dead ) {
        monsters1.remove(index1)
      }
    }
  }

  override def render(graphics: Graphics2D, width: Int, height: Int): Unit = {
    graphics.setBackground(this.backgroundColor)
    graphics.clearRect(0, 0, width, height)

    // TODO starscape or something

    this.render(graphics, this.particles)
    this.render(graphics, this.powerUps)
    this.render(graphics, this.friendlyBullets)
    this.render(graphics, this.enemies)
    this.render(graphics, this.players)

    // render the oxygen
    val oxygenBarWidth = width / 3
    val oxygenBarHeight = 30
    val oxygenBarBorder = 4
    val oxygenBarBorderInner = 6
    graphics.setColor(Color.WHITE)
    graphics.fillRect((width - oxygenBarWidth)/2, height - oxygenBarHeight * 2, oxygenBarWidth, oxygenBarHeight)
    graphics.setColor(Color.BLACK)
    graphics.fillRect((width - oxygenBarWidth)/2 + oxygenBarBorder, height - oxygenBarHeight * 2 + oxygenBarBorder, oxygenBarWidth - oxygenBarBorder* 2, oxygenBarHeight - oxygenBarBorder * 2)
    if( oxygen > maximumOxygen / 7 ) {
      graphics.setColor(Color.WHITE)
    } else {

      graphics.setColor(Color.RED)
    }
    val oxygenWidth = Math.max(0, Math.round((oxygenBarWidth - oxygenBarBorderInner * 2) * oxygen / maximumOxygen)).toInt
    graphics.fillRect((width - oxygenBarWidth)/2 + oxygenBarBorderInner, height - oxygenBarHeight * 2 + oxygenBarBorderInner, oxygenWidth, oxygenBarHeight - oxygenBarBorderInner * 2)


    graphics.setColor(Color.WHITE)
    if( paused ) {
      val pausedString = "PAUSED"
      val fontMetrics = graphics.getFontMetrics
      val pausedBounds = fontMetrics.getStringBounds(pausedString, graphics)
      graphics.drawString(pausedString, (width - pausedBounds.getWidth).toInt/2, (height - pausedBounds.getHeight).toInt/2)
    }
    graphics.drawString(this.distance.toInt.toString, 10, 60)
  }

  def render(graphics: Graphics2D, monsters: ArrayBuffer[Monster]): Unit = {
    for( monster <- monsters) {
      val renderer = this.monsterRendererFactory.lookup(monster)
      renderer.render(graphics, monster)
    }
  }
}
