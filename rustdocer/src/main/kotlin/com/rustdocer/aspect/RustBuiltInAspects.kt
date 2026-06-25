package com.rustdocer.aspect

import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsEnumItem
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.RsTraitItem
import org.rust.lang.core.psi.ext.name

/* ---- shared PSI helpers ---- */

private fun PsiElement.hasOuterAttr(name: String): Boolean {
    val attrOwner = this
    return when (attrOwner) {
        is RsStructItem -> attrOwner.outerAttrList.any { it.metaItem?.name == name }
        is RsEnumItem -> attrOwner.outerAttrList.any { it.metaItem?.name == name }
        is RsFunction -> attrOwner.outerAttrList.any { it.metaItem?.name == name }
        is RsTraitItem -> attrOwner.outerAttrList.any { it.metaItem?.name == name }
        is RsImplItem -> attrOwner.outerAttrList.any { it.metaItem?.name == name }
        else -> false
    }
}

private fun PsiElement.derivedTraits(): List<String> {
    val attrs = when (this) {
        is RsStructItem -> outerAttrList
        is RsEnumItem -> outerAttrList
        else -> return emptyList()
    }
    return attrs.filter { it.metaItem?.name == "derive" }
        .flatMap { attr ->
            attr.metaItem?.metaItemArgs?.metaItemList?.mapNotNull { it.name } ?: emptyList()
        }
}

/* ---- Derive aspect ---- */

/** Detects `#[derive(...)]` attributes and notes which traits are derived. */
object DeriveAspect : RustAspect {
    override val id = "derive"
    override val defaultNote = "Derives: {traits}."

    override fun matches(element: PsiElement): Boolean =
        element.derivedTraits().isNotEmpty()

    override fun tags(element: PsiElement): List<String> = emptyList()

    fun deriveNote(element: PsiElement): String {
        val traits = element.derivedTraits()
        return if (traits.isNotEmpty()) "Derives: ${traits.joinToString(", ")}." else ""
    }
}

/* ---- Serde aspect ---- */

/** Detects serde-related attributes (`#[serde(...)]`, `#[derive(Serialize, Deserialize)]`). */
object SerdeAspect : RustAspect {
    override val id = "serde"
    override val defaultNote = "Supports serde serialization/deserialization."

    private val SERDE_TRAITS = setOf("Serialize", "Deserialize")

    override fun matches(element: PsiElement): Boolean {
        val derived = element.derivedTraits()
        if (derived.any { it in SERDE_TRAITS }) return true
        return element.hasOuterAttr("serde")
    }
}

/* ---- Async trait aspect ---- */

/** Detects `#[async_trait]` attribute on traits and impls. */
object AsyncTraitAspect : RustAspect {
    override val id = "async-trait"
    override val defaultNote = "Uses `#[async_trait]` for async method support in traits."

    override fun matches(element: PsiElement): Boolean =
        element.hasOuterAttr("async_trait")
}

/* ---- Builder aspect ---- */

/** Detects builder pattern: `#[derive(Builder)]` or structs with a `builder()` method. */
object BuilderAspect : RustAspect {
    override val id = "builder"
    override val defaultNote = "Supports the builder pattern for flexible construction."

    override fun matches(element: PsiElement): Boolean {
        if (element !is RsStructItem) return false
        val derived = element.derivedTraits()
        if (derived.any { it == "Builder" }) return true
        // Check if there's a builder() associated function in any impl block
        return false
    }
}

/* ---- Error aspect ---- */

/** Detects Error trait implementations or `#[derive(Error)]` (thiserror). */
object ErrorAspect : RustAspect {
    override val id = "error"
    override val defaultNote = "Implements the `Error` trait for structured error handling."

    override fun matches(element: PsiElement): Boolean {
        if (element is RsStructItem || element is RsEnumItem) {
            val derived = element.derivedTraits()
            if (derived.any { it == "Error" }) return true
        }
        return false
    }
}

/* ---- Unsafe aspect ---- */

/** Detects unsafe fn/impl/trait declarations. */
object UnsafeAspect : RustAspect {
    override val id = "unsafe"
    override val defaultNote = "Contains `unsafe` code — review safety invariants carefully."

    override fun matches(element: PsiElement): Boolean = when (element) {
        is RsFunction -> element.isUnsafe
        is RsImplItem -> element.isUnsafe
        is RsTraitItem -> element.isUnsafe
        else -> false
    }
}
