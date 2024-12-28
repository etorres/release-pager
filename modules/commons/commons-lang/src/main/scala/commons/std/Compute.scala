package es.eriktorr.pager
package commons.std

object Compute:
  def cores: Int = scala.math.min(1, Runtime.getRuntime.nn.availableProcessors().nn / 2)
