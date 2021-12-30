package tff.android

import android.content.Context
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.Normalizer

class Translator {

    private var translations = hashMapOf<String, String>()

    private val alternates = arrayListOf<Alternate>().apply {
        add(Alternate("b", "p"))
        add(Alternate("1", "l"))
        add(Alternate("1", "i"))
        add(Alternate("i", "l"))
        add(Alternate("c", "g"))
        add(Alternate("q", "g"))
        add(Alternate("h", "r"))
        add(Alternate("h", "n"))
        add(Alternate("d", "r"))
        add(Alternate("t", "r"))
        add(Alternate("$", "s"))
        add(Alternate("e", "a"))
        add(Alternate("h", "n"))
        add(Alternate("y", "v"))
        add(Alternate("v", "v"))
        add(Alternate("h", "n"))
        add(Alternate("t", "l"))
        add(Alternate("t", "r"))
        add(Alternate("0", "o"))
    }

    @DelicateCoroutinesApi
    fun initialize(context: Context) {
        GlobalScope.launch {
            val tempMap = hashMapOf<String, String>()
            val duplicateKeys = arrayListOf<String>()
            val duplicateValues = arrayListOf<String>()

            context.applicationContext.assets.open("translations.txt").bufferedReader().readLines().map { line ->
                if (!line.startsWith("#")) {
                    if (!line.contains("=")) {
                        throw RuntimeException("============================ Missing '='line:$line")
                    }

                    line.split("=").let {
                        if (tempMap.containsKey(it.first())) {
                            duplicateKeys.add(it.first())
                        } else if (!it.last().contains("???") && tempMap.containsValue(it.last())) {
                            duplicateValues.add(it.last())
                            tempMap.put(it.first(), it.last())
                        } else {
                            tempMap.put(it.first(), it.last())
                        }
                    }
                }
            }

            Timber.d("Loaded ${tempMap.size} translations")

            if (duplicateKeys.isNotEmpty()) {
                throw RuntimeException("============================ duplicateKeys:$duplicateKeys")
            }

            if (duplicateValues.isNotEmpty()) {
                Timber.e("============================ duplicateValues:$duplicateValues")
            }

            translations = tempMap
        }
    }

    fun translate(elementText: String): TranslationHolder {
        var key = elementText.clean()
        var translation = translations[key]
        if (translation == null) {
            alternates.forEach {
                key = elementText.clean().replace(it.from, it.to)
                translation = translations[key]
                if (translation != null) return@forEach
            }
        }

        Timber.v("translate key:'$key' translation='$translation'")
        return TranslationHolder(key, translation)
    }


    private fun String.clean() = Normalizer.normalize(lowercase(), Normalizer.Form.NFD).filter { it.isLetterOrDigit() }
}

class TranslationHolder(val key: String, val translation: String?)

private class Alternate(val from: String, val to: String)