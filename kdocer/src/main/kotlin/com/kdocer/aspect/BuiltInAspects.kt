package com.kdocer.aspect

import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty

/* ---- shared PSI helpers ---- */

private fun KtAnnotated.hasAnnotation(simpleName: String): Boolean =
    annotationEntries.any { it.shortName?.asString() == simpleName }

private fun KtClassOrObject.superTypeText(): String =
    superTypeListEntries.joinToString(" ") { it.typeReference?.text ?: it.text }

private val LIVE_DATA = Regex("\\b(Mutable)?LiveData\\b")

/* ---- function aspects ---- */

/** Jetpack Compose UI function (`@Composable`). */
object ComposeAspect : KDocAspect {
    override val id = "compose"
    override val defaultNote =
        "Composable function — emits UI and is re-invoked (recomposed) when its inputs change."

    override fun matches(element: KtElement): Boolean =
        element is KtNamedFunction && element.hasAnnotation("Composable")
}

/** A Compose `@Preview` helper. */
object ComposePreviewAspect : KDocAspect {
    override val id = "compose-preview"
    override val defaultNote = "Design-time Compose preview — not used at runtime."

    override fun matches(element: KtElement): Boolean =
        element is KtNamedFunction && element.hasAnnotation("Preview")
}

/* ---- class / object aspects ---- */

/** Android `ViewModel` / `AndroidViewModel` subclass. */
object ViewModelAspect : KDocAspect {
    override val id = "viewmodel"
    override val defaultNote =
        "Android [ViewModel] — survives configuration changes and exposes observable UI state."

    override fun matches(element: KtElement): Boolean =
        element is KtClassOrObject && Regex("\\b(Android)?ViewModel\\b").containsMatchIn(element.superTypeText())
}

/** Kotlin `data class` value holder. */
object DataClassAspect : KDocAspect {
    override val id = "data-class"
    override val defaultNote =
        "Data class — a value holder; `equals`, `hashCode`, `toString` and `copy` are generated."

    override fun matches(element: KtElement): Boolean = element is KtClass && element.isData()
}

/** `sealed` class/interface hierarchy. */
object SealedAspect : KDocAspect {
    override val id = "sealed"
    override val defaultNote =
        "Sealed hierarchy — all subtypes are known at compile time, ideal for exhaustive `when`."

    override fun matches(element: KtElement): Boolean = element is KtClass && element.isSealed()
}

/** `enum class`. */
object EnumAspect : KDocAspect {
    override val id = "enum"
    override val defaultNote = "Enum — a fixed set of named constants."

    override fun matches(element: KtElement): Boolean = element is KtClass && element.isEnum()
}

/** A `companion object`. */
object CompanionAspect : KDocAspect {
    override val id = "companion"
    override val defaultNote =
        "Companion object — holds factory methods and constants for the enclosing class."

    override fun matches(element: KtElement): Boolean =
        element is KtObjectDeclaration && element.isCompanion()
}

/** A top-level `object` singleton (excluding companions). */
object SingletonObjectAspect : KDocAspect {
    override val id = "object"
    override val defaultNote = "Singleton object — a single shared instance."

    override fun matches(element: KtElement): Boolean =
        element is KtObjectDeclaration && !element.isCompanion()
}

/** A utility/helper holder, detected by the `*Util`/`*Utils`/`*Helper` naming convention. */
object UtilAspect : KDocAspect {
    override val id = "util"
    override val defaultNote = "Utility holder — stateless helper functions; not meant to be instantiated."

    override fun matches(element: KtElement): Boolean {
        if (element !is KtClassOrObject) return false
        val name = element.name ?: return false
        return name.endsWith("Util") || name.endsWith("Utils") || name.endsWith("Helper")
    }
}

/* ---- property aspects ---- */

/** A `LiveData`/`MutableLiveData` observable property. */
object LiveDataPropertyAspect : KDocAspect {
    override val id = "livedata"
    override val defaultNote = "Observable [LiveData] state — observe it from the UI to receive updates."

    override fun matches(element: KtElement): Boolean =
        element is KtProperty && element.typeReference?.text?.let { LIVE_DATA.containsMatchIn(it) } == true
}

/** A `StateFlow`/`SharedFlow` observable property. */
object FlowStatePropertyAspect : KDocAspect {
    override val id = "flow-state"
    override val defaultNote = "Observable stream — collect it from a coroutine to receive updates."

    override fun matches(element: KtElement): Boolean =
        element is KtProperty &&
            element.typeReference?.text?.let { Regex("\\b(State|Shared)?Flow\\b").containsMatchIn(it) } == true
}
