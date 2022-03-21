import sbt._

object Dependencies {
  private val scalaPluginLoaderVersion = "0.17.1"
  private val spigotVersion            = "1.17-R0.1-SNAPSHOT"
  private val circeVersion             = "0.14.0"
  private val enumeratumVersion        = "1.7.0"
  private val slickVersion             = "3.3.3"
  private val slickCatsVersion         = "0.10.4"
  private val sl4jVersion              = "1.7.32"
  private val catsVersion              = "2.6.1"

  val resolvers = Seq(
    Resolver.mavenCentral,
    "jitpack" at "https://jitpack.io",
    "spigot-repo" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
  )

  val scalaPluginLoader = "com.github.Jannyboy11.ScalaPluginLoader" % "ScalaLoader" % scalaPluginLoaderVersion

  val spigot = "org.spigotmc" % "spigot-api" % spigotVersion

  val enumeratum = Seq(
    "com.beachape" %% "enumeratum",
    "com.beachape" %% "enumeratum-circe",
    "com.beachape" %% "enumeratum-slick"
  ).map(_ % enumeratumVersion)

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-yaml",
    "io.circe" %% "circe-generic-extras"
  ).map(_ % circeVersion)

  val slick = Seq(
    "com.typesafe.slick" %% "slick"          % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "org.slf4j"           % "slf4j-nop"      % sl4jVersion,
    "com.rms.miu"        %% "slick-cats"     % slickCatsVersion
  )

  val cats = Seq(
    "org.typelevel" %% "cats-core" % catsVersion
  )

  val libraries = enumeratum ++ circe ++ slick ++ cats
}
