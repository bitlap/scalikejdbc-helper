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

import bitlap.scalikejdbc.PostgresSQLSyntaxSupport
import bitlap.scalikejdbc.binders.User.*
import bitlap.scalikejdbc.core.*
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalikejdbc.*

import java.sql.*
import javax.sql.DataSource
import scala.collection.immutable.List

/** @author
 *    梦境迷离
 *  @version 1.0,2023/4/13
 */
class AutoMacrosSpec
    extends AnyFlatSpec
    with Matchers
    with PostgresSQLSyntaxSupport
    with ArrayBinders
    with BeforeAndAfterAll:

  final def jdbcUriTemplate: String = "jdbc:postgresql://localhost:%s/postgres"

  var embeddedPostgres: EmbeddedPostgres = _
  var stmt: Statement                    = _

  override protected def beforeAll(): Unit = {
    embeddedPostgres = EmbeddedPostgres
      .builder()
      .start()

    ConnectionPool.singleton(jdbcUriTemplate.format(embeddedPostgres.getPort), "postgres", "postgres")
    stmt = embeddedPostgres.getPostgresDatabase.getConnection.createStatement()

    val sqls = parseInitFile(getClass.getClassLoader.getResource("test.sql").getFile)
    sqls.foreach(sql => stmt.execute(sql))
  }

  override protected def afterAll(): Unit =
    if (embeddedPostgres != null)
      embeddedPostgres.close()

    if (stmt != null) stmt.close()

  val users3_4 = List(
    User(id = "3", varcharArray = List("333", "333"), decimalArray = Nil, longArray = Nil, intArray = List(1)),
    User(id = "4", varcharArray = List("444", "444"), decimalArray = Nil, longArray = Nil, intArray = List(1))
  )

  "insert with autoNamedValues" should "ok" in {
    DB.localTx { implicit session =>
      given Connection = session.connection

      User.insertUserByNameValues(autoNamedValues(User, users3_4.head)).apply()
    }
    val res = stmt.executeQuery("select int_array,long_array,varchar_array from testdb.t_user where id = '3'")
    res.next()
    val stringTypeBinder = DeriveTypeBinder.array[String, List](_.toList.map(_.toString), Nil)
    val intTypeBinder    = DeriveTypeBinder.array[Int, List](_.toList.map(_.toString.toInt), Nil)
    val intList          = intTypeBinder(res, 1)
    intList shouldEqual List(1)

    val stringList = stringTypeBinder(res, 3)
    stringList shouldEqual List("333", "333")
  }

  "multipleValuesPlus with autoNamedValues" should "ok" in {
    val r = DB.localTx { implicit session =>
      given Connection = session.connection

      val usersNameValues = users3_4.map(u =>
        autoNamedValues(
          User,
          u.copy(id = (u.id.toInt + 10).toString),
          "intArray",
          "parentId",
          "longArray"
        )
      )

      val sql: InsertSQLBuilder =
        insert
          .into(User)
          .columns(autoColumns(User, "intArray", "parentId", "longArray"): _*)
          .multipleValuesPlus(usersNameValues: _*)

      sql.toSQL.statement shouldEqual "insert into testdb.t_user (id, varchar_array, decimal_array) values (?, ?, ?), (?, ?, ?)"
      sql.toSQL.parameters.size shouldEqual 6
      withSQL(sql).update.apply()
    }
    r shouldEqual 2
  }