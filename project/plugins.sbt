// Environnement
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.1")
addSbtPlugin("org.ensime" % "sbt-ensime" % "2.5.1")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")

// Développement
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.3.7")
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.4")
addSbtPlugin("org.duhemm" % "sbt-errors-summary" % "0.6.0")

// ScalaJS
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.25")