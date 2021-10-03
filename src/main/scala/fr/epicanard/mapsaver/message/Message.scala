package fr.epicanard.mapsaver.message

case class Message(components: List[Component]) {
  def withPrefix(prefix: String): Message =
    Message(components.map(Component(prefix) + _))

  def ++(message: Message): Message =
    Message(components ++ message.components)

  def +(component: Component): Message =
    Message(components ++ List(component))

}

object Message {

  val empty: Message = Message(Nil)

  implicit class MessageInterpolation(val s: StringContext) extends AnyVal {
    def msg(params: Any*): Message = {
      val components = s.parts
        .zipAll(params, "", "")
        .foldLeft("") { case (acc, (line, param)) =>
          acc + line.stripMargin + param.toString
        }
        .split("\n")
        .map(Component.apply)
        .toList
      Message(components)
    }
  }
}
