/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.uast.java

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.PsiType
import com.intellij.psi.ResolveResult
import org.jetbrains.uast.*
import org.jetbrains.uast.visitor.UastTypedVisitor
import org.jetbrains.uast.visitor.UastVisitor

class JavaUObjectLiteralExpression(
  override val psi: PsiNewExpression,
  givenParent: UElement?
) : JavaAbstractUExpression(givenParent), UObjectLiteralExpression, UCallExpressionEx, UMultiResolvable {
  override val declaration: UClass by lz { JavaUClass.create(psi.anonymousClass!!, this) }

  override val classReference: UReferenceExpression? by lz {
    psi.classReference?.let { ref ->
      JavaConverter.convertReference(ref, this, null) as? UReferenceExpression
    }
  }

  override val valueArgumentCount: Int
    get() = psi.argumentList?.expressions?.size ?: 0

  override val valueArguments: List<UExpression> by lz {
    psi.argumentList?.expressions?.map { JavaConverter.convertOrEmpty(it, this) } ?: emptyList()
  }

  override fun getArgumentForParameter(i: Int): UExpression? = valueArguments.getOrNull(i)

  override val typeArgumentCount: Int by lz { psi.classReference?.typeParameters?.size ?: 0 }

  override val typeArguments: List<PsiType>
    get() = psi.classReference?.typeParameters?.toList() ?: emptyList()

  override fun resolve(): PsiMethod? = psi.resolveMethod()

  override fun multiResolve(): Iterable<ResolveResult> =
    psi.classReference?.multiResolve(false)?.asIterable() ?: emptyList()

  override fun accept(visitor: UastVisitor) {
    super<UCallExpressionEx>.accept(visitor)
  }

  override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D): R {
    return super<UCallExpressionEx>.accept(visitor, data)
  }
}