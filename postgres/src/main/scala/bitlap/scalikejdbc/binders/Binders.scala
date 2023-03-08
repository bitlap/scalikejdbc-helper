/*
 * Copyright (c) 2023 bitlap
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package bitlap.scalikejdbc.binders

import scalikejdbc.ParameterBinderFactory

import java.sql.Connection

/** @author
 *    梦境迷离
 *  @version 1.0,2023/3/8
 */
trait Binders {
  given list2Array[T](using Connection): ParameterBinderFactory[List[T]] =
    DeriveParameterBinderFactory.array[T, List](_.toArray)

  given seq2Array[T](using Connection): ParameterBinderFactory[Seq[T]] =
    DeriveParameterBinderFactory.array[T, Seq](_.toArray)

  given set2Array[T](using Connection): ParameterBinderFactory[Set[T]] =
    DeriveParameterBinderFactory.array[T, Set](_.toArray)

  given vector2Array[T](using Connection): ParameterBinderFactory[Vector[T]] =
    DeriveParameterBinderFactory.array[T, Vector](_.toArray)

  given map2Json[T](using toJsonString: T => String): ParameterBinderFactory[T] =
    DeriveParameterBinderFactory.json[T](toJsonString)
}