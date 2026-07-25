package com.rustdocer.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.rustdocer.service.RustDocerSettings
import org.rust.lang.core.psi.RsConstant
import org.rust.lang.core.psi.RsEnumItem
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsImplItem
import org.rust.lang.core.psi.RsModItem
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.RsTraitItem
import org.rust.lang.core.psi.RsTypeAlias
import org.rust.lang.core.psi.RsVis
import org.rust.lang.core.psi.ext.RsVisible

object RustValidator {

    fun checkElementIsAllowed(element: PsiElement): Boolean {
        val settings = RustDocerSettings.getInstance()

        val isAllowedType = when (element) {
            is RsFunction -> settings.isAllowedFunction
            is RsStructItem -> settings.isAllowedStruct
            is RsEnumItem -> settings.isAllowedEnum
            is RsTraitItem -> settings.isAllowedTrait
            is RsImplItem -> settings.isAllowedImpl
            is RsModItem -> settings.isAllowedModule
            is RsTypeAlias -> settings.isAllowedTypeAlias
            is RsConstant -> settings.isAllowedConst
            else -> false
        }
        if (!isAllowedType) return false

        if (element is RsVisible) {
            return isVisibilityAllowed(element, settings)
        }
        // RsImplItem does not implement RsVisible directly
        if (element is RsImplItem) return true

        return true
    }

    private fun isVisibilityAllowed(element: RsVisible, settings: RustDocerSettings): Boolean {
        val visText = PsiTreeUtil.getChildOfType(element as PsiElement, RsVis::class.java)?.text?.trim()
            ?: return settings.isAllowedPrivate
        return when {
            visText == "pub" -> settings.isAllowedPub
            visText.contains("super") -> settings.isAllowedPubSuper
            visText.startsWith("pub(") -> settings.isAllowedPubCrate
            else -> settings.isAllowedPrivate
        }
    }
}
