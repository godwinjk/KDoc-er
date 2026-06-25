package com.rustdocer.nlp

import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.RsTraitItem
import org.rust.lang.core.psi.ext.name

/**
 * Resolves cross-references for Rust doc comments: trait implementations,
 * derive macros, and related types.
 */
object RustSeeReferenceResolver {

    fun resolve(element: PsiElement): List<String> {
        val refs = mutableListOf<String>()

        when (element) {
            is RsImplItem -> resolveImpl(element, refs)
            is RsStructItem -> resolveStruct(element, refs)
            is RsTraitItem -> resolveTrait(element, refs)
        }

        return refs
    }

    private fun resolveImpl(impl: RsImplItem, refs: MutableList<String>) {
        // Link to the trait being implemented
        val traitRef = impl.traitRef?.path?.text
        if (traitRef != null) {
            refs += traitRef
        }
        // Link to the type being implemented
        val typeRef = impl.typeReference?.text
        if (typeRef != null && traitRef != null) {
            refs += typeRef
        }
    }

    private fun resolveStruct(struct: RsStructItem, refs: MutableList<String>) {
        // Derive macros create implicit trait implementations
        struct.outerAttrList.forEach { attr ->
            val metaItem = attr.metaItem
            if (metaItem?.name == "derive") {
                metaItem.metaItemArgs?.metaItemList?.forEach { derivedTrait ->
                    derivedTrait.name?.let { refs += it }
                }
            }
        }
    }

    private fun resolveTrait(trait: RsTraitItem, refs: MutableList<String>) {
        // Link to supertraits
        trait.typeParamBounds?.polyboundList?.forEach { bound ->
            bound.bound?.traitRef?.path?.text?.let { refs += it }
        }
    }
}
