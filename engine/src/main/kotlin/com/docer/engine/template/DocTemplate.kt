package com.docer.engine.template

data class DocTemplate(
    val functionDescription: String = "{description}",
    val paramLine: String = "* @param {name} the {noun}",
    val typeParamLine: String = "* @param {name}",
    val returnLine: String = "* @return {description}",

    val propertyDescription: String = "{description}",

    val classDescription: String = "{description}",
    val propertyTagLine: String = "* @property {name} the {noun}",
    val constructorLine: String = "* @constructor {description}",
    val constructorEmptyLine: String = "* @constructor Creates a new {name}",
) {
    companion object {
        val KDOC_DEFAULT = DocTemplate()

        val DARTDOC_DEFAULT = DocTemplate(
            paramLine = "",
            typeParamLine = "",
            returnLine = "Returns {description}.",
            propertyTagLine = "",
            constructorLine = "Creates a new [{name}].",
            constructorEmptyLine = "Creates a new [{name}].",
        )

        val RUSTDOC_DEFAULT = DocTemplate(
            paramLine = "* `{name}` - the {noun}",
            typeParamLine = "* `{name}`",
            returnLine = "{description}",
            propertyTagLine = "* `{name}` - the {noun}",
            constructorLine = "",
            constructorEmptyLine = "",
        )
    }
}
