name          := "MapSaver"
organization  := "fr.epicanard"
version       := "0.0.1-SNAPSHOT"
scalaVersion  := "2.13.6"
useCoursier   := false
scalacOptions  += "-Ymacro-annotations"

resolvers ++= Dependencies.resolvers

libraryDependencies += Dependencies.scalaPluginLoader
libraryDependencies += Dependencies.spigot
libraryDependencies ++= Dependencies.circe
libraryDependencies ++= Dependencies.enumeratum

enablePlugins(BuildInfoPlugin)

assemblyPackageScala / assembleArtifact := false
assembly / assemblyMergeStrategy := {
  case "plugin.yml" =>
    MergeStrategy.first /* always choose our own plugin.yml if we shade other plugins */
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
assembly / assemblyJarName := s"${name.value}-${version.value}.jar"
