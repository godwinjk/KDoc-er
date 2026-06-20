package com.kdocer.template

/**
 * The set of line templates used to assemble a KDoc comment. Every field is a pattern
 * containing `{placeholder}` tokens resolved by [TemplateEngine].
 *
 * Description fields (`*Description`) are bare text placed after the leading `* `.
 * Tag fields (`*Line`) are full comment lines that already include the leading `* `.
 *
 * Defaults reproduce — and gently improve on — the plugin's original hardcoded output,
 * and can be overridden per project via `.kdocer.yaml` or the settings panel.
 */
data class KDocTemplate(
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
        val DEFAULT = KDocTemplate()
    }
}
