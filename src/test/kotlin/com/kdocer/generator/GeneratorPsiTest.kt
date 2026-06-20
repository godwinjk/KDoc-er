package com.kdocer.generator

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.kdocer.service.KDocerSettings
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

/**
 * End-to-end tests that parse real Kotlin and run the generators against the resulting PSI,
 * exercising the nlp + template + aspect engines together.
 */
class GeneratorPsiTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        with(KDocerSettings.getInstance()) {
            isAppendName = true
            isSplittedClassNames = true
            isFrameworkAware = true
            isUsageExample = false
            isConstructorLine = true
            templateFunctionDescription = ""
            templateParam = ""
            templateReturn = ""
            templateClassDescription = ""
            templatePropertyDescription = ""
            templateConstructor = ""
        }
    }

    private fun parse(code: String): PsiFile = myFixture.configureByText("T.kt", code)

    private fun firstFun(code: String): KtNamedFunction =
        PsiTreeUtil.findChildrenOfType(parse(code), KtNamedFunction::class.java).first()

    private fun firstClass(code: String): KtClassOrObject =
        PsiTreeUtil.findChildrenOfType(parse(code), KtClassOrObject::class.java).first()

    private fun firstProperty(code: String): KtProperty =
        PsiTreeUtil.findChildrenOfType(parse(code), KtProperty::class.java).first()

    private fun funDoc(code: String) = NamedFunctionKDocGenerator(project, firstFun(code)).generate()
    private fun classDoc(code: String) = ClassKDocGenerator(project, firstClass(code)).generate()
    private fun propertyDoc(code: String) = PropertyKDocGenerator(project, firstProperty(code)).generate()

    /* ---- NLP: verb-based natural descriptions ---- */

    fun testGetterDescription() {
        val doc = funDoc("fun getUserName(): String = \"\"")
        assertTrue(doc, doc.contains("Returns the user name"))
        // @return now carries a type-derived description rather than being blank/omitted.
        assertTrue(doc, doc.contains("@return the string"))
    }

    fun testInferredExpressionBodyReturn() {
        val doc = funDoc("fun sum(a: Int, b: Int) = a + b")
        // No explicit return type, but the inferred Int still produces a @return.
        assertTrue(doc, doc.contains("@return the int"))
    }

    fun testParamNounNotRepeatedWhenItEqualsName() {
        val doc = funDoc("fun put(key: String) {}")
        assertTrue(doc, doc.contains("@param key"))
        assertFalse(doc, doc.contains("the key"))
    }

    fun testUsageExampleWhenEnabled() {
        KDocerSettings.getInstance().isUsageExample = true
        val doc = funDoc("class User\nfun getUser(): User = User()")
        assertTrue(doc, doc.contains("val user = getUser()"))
    }

    fun testBooleanPredicateDescription() {
        val doc = funDoc("fun isReady(): Boolean = true")
        assertTrue(doc, doc.contains("Returns `true` if ready"))
    }

    fun testCollectionReturnPluralises() {
        val doc = funDoc("fun getUser(): List<String> = emptyList()")
        assertTrue(doc, doc.contains("Returns the users"))
    }

    /* ---- Coroutine / Flow awareness ---- */

    fun testSuspendFlowFunction() {
        val doc = funDoc("class User\nsuspend fun observe(): Flow<User> = TODO()")
        assertTrue(doc, doc.contains("Suspending function"))
        assertTrue(doc, doc.contains("cold [Flow] emitting [User]"))
    }

    /* ---- Aspect layer ---- */

    fun testComposableFunctionNote() {
        val doc = funDoc("annotation class Composable\n@Composable fun Greeting(name: String) {}")
        assertTrue(doc, doc.contains("Composable function"))
    }

    fun testViewModelClassNote() {
        // MainViewModel is declared first so firstClass() selects it (the base is a forward ref).
        val doc = classDoc("class MainViewModel : ViewModel()\nopen class ViewModel")
        assertTrue(doc, doc.contains("Android [ViewModel]"))
    }

    fun testDataClassNoteAndProperties() {
        val doc = classDoc("data class User(val id: Int, val name: String)")
        assertTrue(doc, doc.contains("Data class"))
        assertTrue(doc, doc.contains("@property id"))
        assertTrue(doc, doc.contains("@property name"))
    }

    fun testConstructorLineCanBeDisabled() {
        KDocerSettings.getInstance().isConstructorLine = false
        val doc = classDoc("class Service(repository: String)")
        assertFalse(doc, doc.contains("@constructor"))
        assertTrue(doc, doc.contains("@param repository"))
    }

    fun testConstructorLinePresentByDefault() {
        val doc = classDoc("class Empty")
        assertTrue(doc, doc.contains("@constructor"))
    }

    fun testSingletonObjectNote() {
        val doc = classDoc("object Config")
        assertTrue(doc, doc.contains("Singleton object"))
    }

    fun testUtilClassNote() {
        val doc = classDoc("object DateUtils")
        assertTrue(doc, doc.contains("Utility holder"))
    }

    fun testLiveDataPropertyNote() {
        val doc = propertyDoc("class LiveData<T>\nval state: LiveData<Int> = TODO()")
        assertTrue(doc, doc.contains("Observable [LiveData]"))
    }

    /* ---- Aspect note override via settings is off; default note applies ---- */

    fun testFrameworkAwarenessCanBeDisabled() {
        KDocerSettings.getInstance().isFrameworkAware = false
        val doc = classDoc("data class User(val id: Int)")
        assertFalse(doc, doc.contains("Data class"))
    }
}
