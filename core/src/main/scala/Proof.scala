package chrilves.gadt.prime

sealed abstract class Type {
  @inline final def print: String = Type.print(this)
}
object Type {
  final case class Leaf(text: String)                        extends Type
  final case class Node(label: String, children: List[Type]) extends Type

  @inline def tleaf(t: String): Type =
    Leaf(t)

  @inline def tnode(l: String, c: Type*): Type =
    if (c.isEmpty)
      tleaf(l)
    else
      Node(l, c.toList)

  def print(t: Type): String =
    t match {
      case Leaf(s) =>
        s

      case Node(l, c) =>
        if (c.isEmpty)
          l
        else
          s"$l[${c.map(print).mkString(", ")}]"
    }
}

sealed abstract class Term {
  @inline def print: Pretty = Term.print(this)
}
object Term {
  final case class Leaf(tpe: Type, term: Type)                       extends Term
  final case class Node(tpe: Type, ctor: Type, children: List[Term]) extends Term

  @inline def leaf(tpe: Type, t: String, c: Type*): Term =
    Leaf(tpe, Type.tnode(t, c: _*))

  @inline def leaft(tpe: Type, t: Type): Term =
    Leaf(tpe, t)

  @inline def node(tpe: Type, l: String, c: Term*): Term =
    if (c.isEmpty)
      leaf(tpe, l)
    else
      Node(tpe, Type.tleaf(l), c.toList)

  @inline def nodet(tpe: Type, l: Type, c: Term*): Term =
    if (c.isEmpty)
      leaft(tpe, l)
    else
      Node(tpe, l, c.toList)

  import Pretty._

  def print(term: Term): Pretty = {
    val (tpe, p) =
      term match {
        case Leaf(t, l) =>
          (t, log(l.print + "()"))

        case Node(t, l, c) =>
          (
            t,
            if (c.isEmpty)
              log(l.print + "()")
            else
              Pretty.block(
                log(s"${l.print}(  /* ${t.print} */\n  ") +
                  Pretty.block(
                    c.map(print).sep(log(",\n"))
                  ) +
                  log("\n)")
              )
          )
      }

    Pretty.block(
      log("(") + p + log(s" : ${tpe.print})")
    )
  }

}
