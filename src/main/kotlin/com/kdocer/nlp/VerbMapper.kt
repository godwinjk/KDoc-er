package com.kdocer.nlp

/**
 * Maps a leading verb in an identifier to a natural-language phrase template.
 *
 * The phrase template contains a `{noun}` placeholder that [PhraseBuilder] fills with
 * the remaining words of the name. This is deliberately process-based rather than a
 * per-function hardcode: any name beginning with a known verb gets a sensible sentence.
 *
 * Users can extend or override the mapping via the style sheet (`verbMapping` in
 * `.kdocer.yaml`) — those entries take precedence over the built-in rules.
 */
object VerbMapper {

    private data class Rule(val verbs: Set<String>, val phrase: String)

    private val RULES = listOf(
        Rule(setOf("get", "fetch", "load", "retrieve", "find", "read", "obtain", "of", "from"), "Returns the {noun}"),
        Rule(setOf("set", "update", "save", "store", "write", "put", "assign"), "Sets the {noun}"),
        Rule(setOf("is", "has", "can", "should", "are", "was", "will", "must"), "Returns `true` if {noun}"),
        Rule(setOf("check", "contains", "equals", "matches", "supports", "allows"), "Returns `true` if {noun}"),
        Rule(setOf("create", "build", "make", "new", "generate", "produce", "construct"), "Creates a new {noun}"),
        Rule(setOf("delete", "remove", "clear", "drop", "destroy", "discard", "erase"), "Removes the {noun}"),
        Rule(setOf("add", "append", "insert", "push", "register", "attach"), "Adds the {noun}"),
        Rule(setOf("init", "initialize", "initialise", "setup", "configure", "prepare"), "Initialises the {noun}"),
        Rule(setOf("convert", "parse", "map", "transform", "serialize", "deserialize", "to", "as"), "Converts the {noun}"),
        Rule(setOf("validate", "verify", "ensure", "assert", "require"), "Validates the {noun}"),
        Rule(setOf("calculate", "compute", "count", "sum", "measure"), "Calculates the {noun}"),
        Rule(setOf("handle", "process", "execute", "run", "perform", "apply", "invoke", "call"), "Handles the {noun}"),
        Rule(setOf("send", "emit", "publish", "dispatch", "post", "submit", "notify"), "Sends the {noun}"),
        Rule(setOf("receive", "collect", "consume", "observe", "subscribe", "listen"), "Receives the {noun}"),
        Rule(setOf("start", "begin", "launch", "open", "connect"), "Starts the {noun}"),
        Rule(setOf("stop", "end", "close", "cancel", "finish", "disconnect", "shutdown"), "Stops the {noun}"),
        Rule(setOf("toggle", "switch"), "Toggles the {noun}"),
        Rule(setOf("reset", "refresh", "reload", "sync"), "Resets the {noun}"),
        Rule(setOf("show", "display", "render", "draw", "print"), "Displays the {noun}"),
        Rule(setOf("hide", "dismiss"), "Hides the {noun}"),
        Rule(setOf("filter", "select", "query", "search"), "Filters the {noun}"),
        Rule(setOf("sort", "order", "arrange"), "Sorts the {noun}"),
        Rule(setOf("copy", "clone", "duplicate"), "Copies the {noun}"),
        Rule(setOf("merge", "combine", "join", "concat"), "Merges the {noun}"),
        Rule(setOf("enable"), "Enables the {noun}"),
        Rule(setOf("disable"), "Disables the {noun}"),
    )

    private val BY_VERB: Map<String, String> =
        RULES.flatMap { rule -> rule.verbs.map { it to rule.phrase } }.toMap()

    /**
     * Resolves the leading verb of [words] to a phrase template and the remaining noun words.
     * [extra] (custom verb mappings from the style sheet) take priority over built-ins.
     * Returns `null` when the first word is not a recognised verb.
     */
    fun resolve(words: List<String>, extra: Map<String, String> = emptyMap()): Pair<String, List<String>>? {
        if (words.isEmpty()) return null
        val first = words.first()
        val phrase = extra[first] ?: BY_VERB[first] ?: return null
        return phrase to words.drop(1)
    }
}
