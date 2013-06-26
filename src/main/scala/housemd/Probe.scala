package housemd

import scala.Predef._


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class Probe(val klass: String, val filter: MethodFilter, val options: Int)

object Probe {
  def apply(klass: String, filter: MethodFilter, options: Int) =
    new Probe(klass, filter, options) with Chase with Cameron
}

