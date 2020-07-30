package com.kdocer.generator

/**
 * Test class
 *
 * @constructor Create empty Test class

 */
class TestClass {
    val validDeclaration =0

    val anotherDeclaration =""

    fun testClass(){

    }

    /**
     * Another class
     *
     * @param param
     * @param param1
     * @return
     */
    fun anotherClass(param:Int, param1:String): String=""

    class SomeClass{
        fun someMethod(){
            class InnerClass{

            }
        }
    }

}