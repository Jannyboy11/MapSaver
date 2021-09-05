import sbt._

object Dependencies {
  private val scalaPluginLoaderVersion  = "0.17.1"
  private val spigotVersion             = "1.17-R0.1-SNAPSHOT"
  private val circeVersion              = "0.14.0"
  private val enumeratumVersion         = "1.7.0"
  private val slickVersion              = "3.3.3"
  private val sl4jVersion               = "1.6.4"
  private val catsVersion               = "2.3.0"

  val resolvers = Seq(
    Resolver.mavenCentral,
    "jitpack"     at "https://jitpack.io",
    "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
  )

  lazy val scalaPluginLoader = "com.github.Jannyboy11.ScalaPluginLoader" % "ScalaLoader" % scalaPluginLoaderVersion % "provided"
  lazy val spigot            = "org.spigotmc"                            % "spigot-api"  % spigotVersion            % "provided"
  lazy val enumeratum        = Seq(
    "com.beachape" %% "enumeratum",
    "com.beachape" %% "enumeratum-circe",
    "com.beachape" %% "enumeratum-slick"
  ).map(_ % enumeratumVersion)

  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-yaml",
    "io.circe" %% "circe-generic-extras"
  ).map(_ % circeVersion)

  lazy val slick = Seq(
    "com.typesafe.slick" %% "slick"          % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "org.slf4j"          %  "slf4j-nop"      % sl4jVersion,
  )

  lazy val cats = Seq(
    "org.typelevel" %% "cats-core" % catsVersion
  )
}
