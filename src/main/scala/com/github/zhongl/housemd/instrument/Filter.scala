package com.github.zhongl.housemd.instrument


/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
trait Filter extends (Class[_] => Boolean) with ((Class[_], String) => Boolean)

