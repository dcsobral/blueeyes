name := "blueeyes"

version := "0.3.28"

organization := "com.reportgrid"

scalaVersion := "2.8.1"

libraryDependencies ++= Seq(
  "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.1",
  "com.thoughtworks.paranamer"  % "paranamer"           % "2.3",
  "commons-codec"               % "commons-codec"       % "1.5",
  "joda-time"                   % "joda-time"           % "1.6.2",
  "net.lag"                     % "configgy"            % "2.0.0",
  "org.jboss.netty"             % "netty"               % "3.2.4.Final",
  "org.mongodb"                 % "mongo-java-driver"   % "2.5.3",
  "org.scalaz"                  %% "scalaz-core"        % "6.0-SNAPSHOT",
  "org.xlightweb"               % "xlightweb"           % "2.13.2",
  "rhino"                       % "js"                  % "1.7R2",
  "org.scala-tools.testing"     %% "specs"              % "1.6.7"         % "test",
  "org.scala-tools.testing"     %% "scalacheck"         % "1.8"           % "test",
  "org.mockito"                 % "mockito-all"         % "1.8.5"         % "test"
)

resolvers ++= Seq(
  "Scala Repo Releases" at        "http://scala-tools.org/repo-releases/",
  "Scala-tools.org Repository" at "http://scala-tools.org/repo-snapshots/",
  "JBoss Releases" at             "http://repository.jboss.org/nexus/content/groups/public/",
  "Sonatype Releases" at          "http://oss.sonatype.org/content/repositories/releases",
  "Nexus Scala Tools" at          "http://nexus.scala-tools.org/content/repositories/releases",
  "Maven Repo 1" at               "http://repo1.maven.org/maven2/",
  "Akka Repository" at            "http://akka.io/repository/"
)

parallelExecution in Test := false

publishTo <<= (version) { version: String =>
  val nexus = "http://nexus.scala-tools.org/content/repositories/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus+"snapshots/") 
  else                                   Some("releases"  at nexus+"releases/")
}

credentials := Credentials(Path.userHome / ".ivy2" / ".credentials") :: Nil