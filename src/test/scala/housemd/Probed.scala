package housemd

import java.lang.reflect.Method

case class Probed(clazz: Class[_]) {
  val instance = clazz.newInstance.asInstanceOf[AnyRef]

  val loader = clazz.getClassLoader

  def method(name: String, classes: Class[_]*) = new Invocation(instance, clazz.getMethod(name, classes: _*))
}


class Invocation(o: Object, method: Method) {
  def invoke(parameters: AnyRef*) = method.invoke(o, parameters: _*)
}
