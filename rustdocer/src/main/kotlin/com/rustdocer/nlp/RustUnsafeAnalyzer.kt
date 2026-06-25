package com.rustdocer.nlp

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsBlockExpr
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsTraitItem
import org.rust.lang.core.psi.ext.block

/**
 * Detects unsafe patterns in Rust code:
 * - `unsafe fn` keyword on functions
 * - `unsafe impl` blocks
 * - `unsafe trait` declarations
 * - `unsafe` blocks within function bodies
 */
object RustUnsafeAnalyzer {

    data class UnsafeInfo(
        val isUnsafe: Boolean,
        val hasUnsafeBlocks: Boolean,
    ) {
        val shouldEmitSafetySection: Boolean get() = isUnsafe
    }

    fun analyze(element: PsiElement): UnsafeInfo = when (element) {
        is RsFunction -> analyzeFunction(element)
        is RsImplItem -> analyzeImpl(element)
        is RsTraitItem -> analyzeTrait(element)
        else -> UnsafeInfo(isUnsafe = false, hasUnsafeBlocks = false)
    }

    private fun analyzeFunction(function: RsFunction): UnsafeInfo {
        val isUnsafe = function.isUnsafe
        val body = function.block
        val hasUnsafeBlocks = body != null &&
            PsiTreeUtil.collectElementsOfType(body, RsBlockExpr::class.java).any { it.unsafe != null }
        return UnsafeInfo(isUnsafe, hasUnsafeBlocks)
    }

    private fun analyzeImpl(impl: RsImplItem): UnsafeInfo {
        val isUnsafe = impl.isUnsafe
        return UnsafeInfo(isUnsafe, hasUnsafeBlocks = false)
    }

    private fun analyzeTrait(trait: RsTraitItem): UnsafeInfo {
        val isUnsafe = trait.isUnsafe
        return UnsafeInfo(isUnsafe, hasUnsafeBlocks = false)
    }
}
