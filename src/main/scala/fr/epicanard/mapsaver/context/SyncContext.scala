package fr.epicanard.mapsaver.context

import cats.arrow.FunctionK
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import slick.dbio.DBIO

import java.util.concurrent.{Callable, CompletableFuture}
import scala.concurrent.Future
import scala.jdk.FutureConverters._

case class SyncContext(plugin: Plugin)

object SyncContext {

  implicit val fromFutureToDBIO: FunctionK[Future, DBIO] = new FunctionK[Future, DBIO] {
    def apply[A](fa: Future[A]): DBIO[A] = DBIO.from(fa)
  }

  def sync[F[_], T](callable: Callable[T])(implicit syncContext: SyncContext, transform: FunctionK[Future, F]): F[T] = {
    val result = Bukkit.getScheduler.callSyncMethod(syncContext.plugin, callable)
    val future = CompletableFuture.supplyAsync(() => result.get).asScala
    transform(future)
  }
}
