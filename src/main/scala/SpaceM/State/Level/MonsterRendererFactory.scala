package SpaceM.State.Level

/**
 * Created by chris on 23/08/14.
 */
trait MonsterRendererFactory {
  def lookup(monster: Monster): MonsterRenderer;
}
