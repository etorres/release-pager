package es.eriktorr.pager
package application

import com.monovore.decline.Opts

final case class ReleaseSenderParams(verbose: Boolean)

object ReleaseSenderParams:
  def opts: Opts[ReleaseSenderParams] = Opts
    .flag("verbose", short = "v", help = "Print extra metadata to the logs.")
    .orFalse
    .map(ReleaseSenderParams.apply)
