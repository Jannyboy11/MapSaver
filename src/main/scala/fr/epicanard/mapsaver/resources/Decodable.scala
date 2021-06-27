package fr.epicanard.mapsaver.resources

import io.circe.Decoder

type Decodable[F[_], T] = Decoder[T] ?=> F[T]
