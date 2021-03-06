/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.v3_5.ast

import org.neo4j.cypher.internal.v3_5.expressions._
import org.neo4j.cypher.internal.v3_5.expressions.functions.Exists
import org.neo4j.cypher.internal.v3_5.util.test_helpers.CypherTestSupport
import org.neo4j.cypher.internal.v3_5.util.{DummyPosition, InputPosition}

import scala.language.implicitConversions

trait AstConstructionTestSupport extends CypherTestSupport {
  protected val pos = DummyPosition(0)

  implicit def withPos[T](expr: InputPosition => T): T = expr(pos)

  def varFor(name: String): Variable = Variable(name)(pos)

  def lblName(s: String): LabelName = LabelName(s)(pos)

  def hasLabels(v: String, label: String): HasLabels =
    HasLabels(varFor(v), Seq(lblName(label)))(pos)

  def exists(e: Expression): FunctionInvocation =
    FunctionInvocation(FunctionName(Exists.name)(e.position), e)(e.position)

  def prop(variable: String, propKey: String): Property = Property(varFor(variable), PropertyKeyName(propKey)(pos))(pos)

  def propEquality(variable: String, propKey: String, intValue: Int): Equals =
    Equals(prop(variable, propKey), literalInt(intValue))(pos)

  def propLessThan(variable: String, propKey: String, intValue: Int): LessThan =
    LessThan(prop(variable, propKey), literalInt(intValue))(pos)

  def literalInt(intValue: Int): SignedDecimalIntegerLiteral =
    SignedDecimalIntegerLiteral(intValue.toString)(pos)

  def literalFloat(floatValue: Double): DecimalDoubleLiteral =
    DecimalDoubleLiteral(floatValue.toString)(pos)

  def literalList(expressions: Expression*): ListLiteral =
    ListLiteral(expressions.toSeq)(pos)

  def literalIntList(intValues: Int*): ListLiteral =
    ListLiteral(intValues.toSeq.map(literalInt))(pos)

  def literalFloatList(floatValues: Double*): ListLiteral =
    ListLiteral(floatValues.toSeq.map(literalFloat))(pos)

  def literalIntMap(keyValues: (String, Int)*): MapExpression =
    MapExpression(keyValues.map {
      case (k, v) => (PropertyKeyName(k)(pos), SignedDecimalIntegerLiteral(v.toString)(pos))
    })(pos)

  def listOf(expressions: Expression*): ListLiteral = ListLiteral(expressions)(pos)

  def mapOf(keysAndValues: (String, Expression)*): MapExpression = MapExpression(keysAndValues.map {
    case (k, v) => PropertyKeyName(k)(pos) -> v
  })(pos)

  def TRUE: Expression = True()(pos)

  def function(name: String, args: Expression*): FunctionInvocation =  FunctionInvocation(FunctionName(name)(pos),
                                                                                          distinct = false, args.toIndexedSeq)(pos)

  def function(name: String, deprecated: Boolean, args: Expression*): FunctionInvocation =  FunctionInvocation(FunctionName(name)(pos),
                                                                                          distinct = false, args.toIndexedSeq, deprecated)(pos)
}
