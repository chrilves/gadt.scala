package chrilves.gadt.prime

final case class Position(current: Int, offset: Int):
  inline def save: Position =
    copy(offset = current)

final case class State(position: Position, output: String)

sealed abstract class Pretty extends (Position => State) { self =>
  def apply(position: Position): State

  inline final def run: String =
    apply(Position(0, 0)).output

  inline final def +(other: Pretty): Pretty =
    Pretty { (pos: Position) =>
      val State(p0, o0) = self(pos)
      val State(p1, o1) = other(p0)
      State(p1, o0 + o1)
    }

  inline final def local: Pretty = Pretty { (pos: Position) =>
    val State(Position(c, _), s) = self(pos)
    State(Position(c, pos.offset), s)
  }

  inline final def block: Pretty = Pretty { (pos: Position) =>
    val State(Position(c, _), s) = self(pos.save)
    State(Position(c, pos.offset), s)
  }
}

object Pretty {
  inline def apply(f: Position => State): Pretty =
    new Pretty {
      def apply(pos: Position): State = f(pos)
    }

  val empty: Pretty = Pretty { (pos: Position) => State(pos, "") }

  val newLine: Pretty = Pretty { (pos: Position) =>
    State(pos.copy(current = pos.offset), s"\n${" " * pos.offset}")
  }

  inline def offset: Pretty = Pretty { (pos: Position) => State(pos.save, "") }

  inline def local(p: Pretty): Pretty =
    p.local

  inline def block(p: Pretty): Pretty =
    p.block

  def log(s: String): Pretty = Pretty { (pos: Position) =>
    val output: String =
      s.replaceAll("[\n]", s"\n${" " * pos.offset}")

    val newCurrent: Int =
      "[\n]([^\n]*$)".r
        .findFirstMatchIn(output)
        .map(_.group(1).length)
        .getOrElse(pos.current + s.length)

    State(pos.copy(current = newCurrent), output)
  }

  extension (self: List[Pretty])
    inline def sep(s: Pretty): Pretty =
      self
        .reduceOption(_ + s + _)
        .getOrElse(Pretty.empty)

    inline def concat: Pretty =
      self.foldLeft(Pretty.empty)(_ + _)
}
