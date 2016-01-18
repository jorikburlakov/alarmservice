import com.github.play2war.plugin.Play2WarKeys
import com.github.play2war.plugin.Play2WarPlugin
import com.github.play2war.plugin._

name := """test"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(Play2WarPlugin.play2WarSettings: _*)
  .settings(Play2WarKeys.servletVersion := "3.0")

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.firebase" % "firebase-client-android" % "1.1.1"
)

