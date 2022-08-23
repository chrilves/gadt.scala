package chrilves.gadt.prime

import org.scalajs.dom._
import org.scalajs.dom.Blob
import typed.web._
import typed.web.Html
import typed.web.syntax.html._

import scala.scalajs.js

object PrimeWeb extends WebApp:
  type Model = String
  val initialModel: Model = ""

  enum Msg:
    case NewInput(str: String)
    case Download

  inline def toInt(s: String): Option[Int] =
    try (Some(s.toInt))
    catch { case _: NumberFormatException => None }

  def documentReactions(model: Model): List[Reaction[Option[Msg]]] = Nil

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def update(message: Msg, model: Model): Model =
    message match
      case Msg.NewInput(str) => str
      case Msg.Download =>
        toInt(model) match {
          case Some(n) if n >= 1 =>
            val file: String =
              Prime.file(n).run
            val blob: Blob =
              new Blob(
                js.Array(file),
                js.Dynamic.literal(`type` = "text/plain").asInstanceOf[BlobPropertyBag]
              )
            WebApp.download(blob, s"Prime$n.scala")

          case _ =>
            window.alert(s"'$model' is not a positive integer!")
        }

        model

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  def view(model: Model): Html[Option[Msg]] =
    span()(
      input(value(model), oninput(x => Some(Msg.NewInput(x))))(),
      toInt(model) match {
        case Some(n) if n >= 1 =>
          button(onclick(Some(Msg.Download)))(text(s"Download 'Prime$n.scala'"))
        case _ =>
          text("")
      }
    )

  def main(args: Array[String]): Unit =
    runMain("prime-web", WebApp.UseDifference)
