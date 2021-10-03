package fr.epicanard.mapsaver.message

import java.util.regex.Pattern

object Replacer {
  private val replacePattern: Pattern = Pattern.compile("\\{([a-zA-Z]+)}")

  implicit class ReplacerExtension(val value: String) {
    def replace(elems: (String, String)*): String = {
      val args    = Map.from(elems)
      val matcher = replacePattern.matcher(value)
      val result  = new StringBuffer(value.length)
      while (matcher.find) {
        val key = matcher.group(1)
        matcher.appendReplacement(result, "")
        result.append(args.getOrElse(key, s"{$key}"))
      }
      matcher.appendTail(result)
      result.toString
    }
  }
}
