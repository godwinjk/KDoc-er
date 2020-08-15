package com.kdocer.util

import com.intellij.psi.PsiElement
import com.kdocer.service.KDocerSettings
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.psi.psiUtil.isProtected
import org.jetbrains.kotlin.psi.psiUtil.isPublic

/**
 * Created by Godwin on 8/8/2020 8:15 PM.
 *
 * @author : Godwin Joseph Kurinjikattu
 * @since : 2020
 */
object Validator {
    fun checkElementIsAllowed(element: PsiElement): Boolean {
        val settings = KDocerSettings.getInstance()
        if (element is KtDeclaration && element.docComment != null && settings.isAllowedKeepDoc) return false
        if (element is KtClassOrObject && settings.isAllowedClass) {
            if (hasModifier(element, "internal") && settings.isAllowedInternal ||
                isPublic(element) && settings.isAllowedPublic ||
                isProtected(element) && settings.isAllowedProtected ||
                isPrivate(element) && settings.isAllowedPrivate
            ) return true
        } else if ((element is KtNamedFunction && settings.isAllowedFun)) {
            if (hasModifier(element, "override") && !settings.isAllowedOverride) return false
            if ((hasModifier(element, "internal") && settings.isAllowedInternal ||
                        isPublic(element) && settings.isAllowedPublic ||
                        isProtected(element) && settings.isAllowedProtected ||
                        isPrivate(element) && settings.isAllowedPrivate)
            ) return true
        } else if (element is KtProperty && settings.isAllowedField) {
            if (hasModifier(element, "override") && !settings.isAllowedOverride) return false
            if ((hasModifier(element, "internal") && settings.isAllowedInternal ||
                        isPublic(element) && settings.isAllowedPublic ||
                        isProtected(element) && settings.isAllowedProtected ||
                        isPrivate(element) && settings.isAllowedPrivate)
            ) return true
        }
        return false
    }

    fun hasModifier(element: KtModifierListOwner, text: String): Boolean {
        return (element.modifierList?.firstChild?.text == text ||
                element.modifierList?.lastChild?.text == text
                )
    }

    fun isPrivate(element: KtModifierListOwner): Boolean {
        return element.isPrivate()
    }

    fun isPublic(element: KtModifierListOwner): Boolean {
        return element.isPublic
    }

    fun isProtected(element: KtModifierListOwner): Boolean {
        return element.isProtected()
    }

    fun isNameNeedsSplit(): Boolean {
        val settings = KDocerSettings.getInstance()
        return settings.isSplittedClassNames
    }
}