// Environnement
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

// Développement
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.0.14")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.2")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

// ScalaJS
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.2.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.0")