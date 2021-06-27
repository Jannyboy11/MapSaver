import sbt._

object Dependencies {
  private val scalaPluginLoaderVersion  = "0.17.1"
  private val spigotVersion             = "1.17-R0.1-SNAPSHOT"
  private val circeVersion              = "0.14.0"

  val resolvers = Seq(
    Resolver.mavenCentral,
    "jitpack"     at "https://jitpack.io",
    "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
  )

  lazy val scalaPluginLoader = "com.github.Jannyboy11.ScalaPluginLoader" % "ScalaLoader" % scalaPluginLoaderVersion % "provided"
  lazy val spigot            = "org.spigotmc"                            % "spigot-api"  % spigotVersion            % "provided"

  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-yaml"
  ).map(_ % circeVersion)
}
