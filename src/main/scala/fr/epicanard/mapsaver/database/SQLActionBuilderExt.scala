package fr.epicanard.mapsaver.database

import slick.jdbc.{PositionedParameters, SQLActionBuilder}

object SQLActionBuilderExt {

  implicit class SQLActionBuilderConcat(val base: SQLActionBuilder) {
    def ++(add: SQLActionBuilder): SQLActionBuilder =
      SQLActionBuilder(
        base.queryParts ++ add.queryParts,
        (p: Unit, pp: PositionedParameters) => {
          base.unitPConv.apply(p, pp)
          add.unitPConv.apply(p, pp)
        }
      )

    def +?(addOpt: Option[SQLActionBuilder]): SQLActionBuilder =
      addOpt.map(base ++ _).getOrElse(base)
  }
}
