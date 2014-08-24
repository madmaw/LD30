package SpaceM

import javax.sound.sampled.{LineEvent, LineListener, AudioSystem, Clip}

/**
 * Created by chris on 24/08/14.
 */
class SoundEffect(paths: Array[String]) {

  def play(): Unit = {
    return play((Math.random() * paths.length).toInt);
  }

  def play(variant: Int): Unit = {
    val index = variant % paths.length
    val path = paths(index)
    val clip = AudioSystem.getClip()
    val url = getClass.getClassLoader.getResource(path)
    val ins = AudioSystem.getAudioInputStream(url)
    clip.open(ins)
    clip.start()
  }
}
