import sbt._

object Dependencies {
  lazy val thirdPartyDeps = Seq(
    "com.github.pathikrit" %% "better-files" % "3.9.1",
    "com.destroystokyo.paper" % "paper-api" % "1.15.2-R0.1-20200807.011053-317" % "provided"
  )
}
