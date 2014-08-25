package SpaceM.State.Level

/**
 * Created by chris on 23/08/14.
 */
class HardCodedRendererFactory(
  playerRenderer: MonsterRenderer,
  jellyRenderer: MonsterRenderer,
  powerUpOxygenRenderer: MonsterRenderer,
  freezeRenderer: MonsterRenderer,
  spreadRenderer: MonsterRenderer,
  rapidRenderer: MonsterRenderer,
  bulletRenderer: MonsterRenderer,
  thrustRenderer: MonsterRenderer,
  defaultRenderer: MonsterRenderer
) extends MonsterRendererFactory {

  var asteroidRenderer = new AsteroidRenderer()

  override def lookup(monster: Monster): MonsterRenderer = {
    if( monster.monsterType == MonsterType.Player ) {
      return this.playerRenderer
    } else if( monster.monsterType == MonsterType.Jelly ) {
      return this.jellyRenderer
    } else if( monster.monsterType == MonsterType.Asteroid || monster.monsterType == MonsterType.Fragment ) {
      return this.asteroidRenderer
    } else if( monster.monsterType == MonsterType.PowerUpOxygen ) {
      return this.powerUpOxygenRenderer
    } else if( monster.monsterType == MonsterType.PowerUpFreeze ) {
      return this.freezeRenderer
    } else if( monster.monsterType == MonsterType.PowerUpSpread ) {
      return this.spreadRenderer
    } else if( monster.monsterType == MonsterType.PowerUpRapid ) {
      return this.rapidRenderer
    } else if( monster.monsterType == MonsterType.Bullet) {
      return this.bulletRenderer
    } else if (monster.monsterType == MonsterType.Thrust ) {
      return this.thrustRenderer
    } else {
      return this.defaultRenderer
    }
  }

}
