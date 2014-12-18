package com.github.zhongl.housemd.misc

import com.cedarsoftware.util.io.JsonWriter

/**
 * @author <a href="mailto:eagleinfly@gmail.com">eagleinfly<a>
 */
object ObjUtils {
  val jsonFormater = (ref: AnyRef) => JsonWriter.objectToJson(ref)
  val toStringFormater = (ref: AnyRef) => Option(ref).getOrElse("null").toString

  var formatter = toStringFormater

  def useJsonFormat() = {
    formatter = jsonFormater
  }

  def useToStringFormat() = {
    formatter = toStringFormater
  }

  def toString(obj: AnyRef) = {
    formatter(obj)
  }
}
