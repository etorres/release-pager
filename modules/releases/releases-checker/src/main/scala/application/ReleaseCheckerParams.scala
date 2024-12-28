package es.eriktorr.pager
package application

import com.monovore.decline.Opts

final case class ReleaseCheckerParams(verbose: Boolean)

object ReleaseCheckerParams:
  def opts: Opts[ReleaseCheckerParams] = Opts
    .flag("verbose", short = "v", help = "Print extra metadata to the logs.")
    .orFalse
    .map(ReleaseCheckerParams.apply)
