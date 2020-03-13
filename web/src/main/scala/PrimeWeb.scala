package chrilves.gadt.prime

import org.scalajs.dom._
import org.scalajs.dom.Blob
import typed.web._
import typed.web.Html
import typed.web.syntax.html._

import scala.scalajs.js

object PrimeWeb extends WebApp {
  type Model = String
  val initialModel: Model = ""

  sealed abstract class Msg
  object Msg {
    final case class NewInput(str: String) extends Msg
    final case object Download extends Msg
  }

  @inline def toInt(s: String): Option[Int] =
    try (Some(s.toInt))
    catch { case _: NumberFormatException => None }

  def documentReactions(model: Model): List[Reaction[Option[Msg]]] = Nil

  def update(message: Msg, model: Model): Model =
    message match {
      case Msg.NewInput(str) => str
      case Msg.Download =>
        toInt(model) match {
          case Some(n) if n >= 1 =>
            val file: String =
              Prime.file(n).run
            val blob: Blob =
              new Blob(js.Array(file), BlobPropertyBag("text/plain"))
            WebApp.download(blob, s"Prime$n.scala")

          case _ =>
            window.alert(s"'$model' is not a positive integer!")
        }

        model
    }
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  def view(model: Model): Html[Option[Msg]] =
    div()(
      h1()(
        a(
          href("https://en.wikipedia.org/wiki/Generalized_algebraic_data_type")
        )(text("Generalized Algebraic Data Types (GADT)")),
        text(" example: "),
        a(href("https://en.wikipedia.org/wiki/Prime_number"))(
          text("Prime Numbers.")
        )
      ),
      p()(
        text("""This is a demonstration of the expressive power of GADTs.
            |Please enter a positive integer, prime or not, then download
            |the file (the button only appears when there is a positive
            |integer in the input box). The file contains a valid Scala
            |program (run it to be sure there is no trick). Then follow
            |the intructions in the file. You can try as many numbers as
            |you like.
          """.stripMargin)
      ),
      p()(text("Enter a positive integer")),
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
}
