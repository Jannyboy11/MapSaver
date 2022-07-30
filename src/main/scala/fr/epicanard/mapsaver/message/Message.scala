package fr.epicanard.mapsaver.message

case class Message(components: List[Component]) {
  def withPrefix(prefix: String): Message =
    Message(components.map(Component(prefix) + _))

  def ++(message: Message): Message =
    Message(components ++ message.components)

  def +(component: Component): Message =
    Message(components ++ List(component))

  def +?(maybeComponent: Option[Component]): Message =
    maybeComponent.map(+).getOrElse(this)

}

object Message {

  val empty: Message = Message(Nil)

  def apply(input: String): Message = Message(input.split("\n").map(Component.apply).toList)

  implicit class MessageInterpolation(val s: StringContext) extends AnyVal {
    def msg(params: Any*): Message =
      Message(
        s.parts
          .zipAll(params, "", "")
          .foldLeft("") { case (acc, (line, param)) =>
            acc + line.stripMargin + s"$param"
          }
      )
  }
}
