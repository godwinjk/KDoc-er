package com.dartdocer.util

import com.intellij.psi.PsiElement
import com.dartdocer.service.DartDocerSettings
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartExtensionDeclaration
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative
import com.jetbrains.lang.dart.psi.DartGetterDeclaration
import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.psi.DartMixinDeclaration
import com.jetbrains.lang.dart.psi.DartSetterDeclaration
import com.jetbrains.lang.dart.psi.DartVarDeclarationList
import com.jetbrains.lang.dart.psi.DartEnumDefinition

/**
 * Checks if a Dart PSI element qualifies for doc generation based on settings.
 * Dart visibility is determined by underscore prefix (private) vs no prefix (public).
 */
object DartValidator {

    fun checkElementIsAllowed(element: PsiElement): Boolean {
        val settings = DartDocerSettings.getInstance()

        val name = when (element) {
            is DartComponent -> element.name
            else -> return false
        }

        // Visibility check: Dart uses underscore prefix for private members
        val isPrivate = name?.startsWith("_") == true
        if (isPrivate && !settings.isAllowedPrivate) return false
        if (!isPrivate && !settings.isAllowedPublic) return false

        // Override check
        if (!settings.isAllowedOverride && isOverride(element)) return false

        // Element type check
        return when (element) {
            is DartClassDefinition -> settings.isAllowedClass
            is DartEnumDefinition -> settings.isAllowedClass
            is DartFunctionDeclarationWithBodyOrNative -> settings.isAllowedFunction
            is DartMethodDeclaration -> settings.isAllowedFunction
            is DartGetterDeclaration -> settings.isAllowedField
            is DartSetterDeclaration -> settings.isAllowedField
            is DartVarDeclarationList -> settings.isAllowedField
            is DartExtensionDeclaration -> settings.isAllowedExtension
            is DartMixinDeclaration -> settings.isAllowedMixin
            else -> false
        }
    }

    private fun isOverride(element: PsiElement): Boolean {
        if (element !is DartComponent) return false
        val metadataList = element.metadataList
        return metadataList.any { metadata ->
            metadata.referenceExpression?.text == "override"
        }
    }
}
