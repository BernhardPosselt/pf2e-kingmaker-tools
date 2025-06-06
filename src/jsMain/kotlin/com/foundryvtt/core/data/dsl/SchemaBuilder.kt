package com.foundryvtt.core.data.dsl

import at.posselt.pfrpg2e.toCamelCase
import at.posselt.pfrpg2e.utils.toRecord
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.data.fields.ArrayField
import com.foundryvtt.core.data.fields.ArrayFieldOptions
import com.foundryvtt.core.data.fields.BooleanField
import com.foundryvtt.core.data.fields.DataField
import com.foundryvtt.core.data.fields.DataFieldContext
import com.foundryvtt.core.data.fields.DataFieldOptions
import com.foundryvtt.core.data.fields.DataSchema
import com.foundryvtt.core.data.fields.NumberField
import com.foundryvtt.core.data.fields.NumberFieldOptions
import com.foundryvtt.core.data.fields.ObjectField
import com.foundryvtt.core.data.fields.SchemaField
import com.foundryvtt.core.data.fields.StringField
import com.foundryvtt.core.data.fields.StringFieldOptions
import js.objects.Record
import js.objects.recordOf
import kotlin.enums.enumEntries

/**
 * DSL that abstracts Foundry's Schema settings. Each type allows you to configure
 * its options by passing a lambda, e.g.
 * int("increaseWatchActorNumber") {
 *     blank = false
 * }
 *
 * By default, every field is required; if you want to make a field optional, use:
 *
 * int("increaseWatchActorNumber") {
 *     required = false
 * }
 *
 * This will turn into
 *
 *    increaseWatchActorNumber: NumberField({required: true, integer: true})
 *
 * For form parsing, you either want an option to be required (non-nullable type)
 * or to map onto null if it's absent. Because this requires 3 properties, there is a
 * nullable flag for both number and string fields, e.g.:
 *
 * string("proxyRandomEncounterTableUuid", nullable = true)
 *
 * This will turn into
 *
 *     proxyRandomEncounterTableUuid: StringField({required: false, blank: false, initial: null, nullable: true})
 *
 * Usage:
 * class CampingSettingsDataModel(
 *     value: AnyObject,
 *     options: DocumentConstructionContext
 * ) : DataModel(value, options) {
 *     companion object {
 *         @Suppress("unused")
 *         @JsStatic
 *         fun defineSchema() = buildSchema {
 *             int("gunsToClean")
 *             string("restRollMode") {
 *                 choices = arrayOf("none", "one", "one-every-4-hours")
 *             }
 *             double("increaseWatchActorNumber")
 *             stringArray("actorUuidsNotKeepingWatch")
 *             string("proxyRandomEncounterTableUuid", nullable = true)
 *             boolean("ignoreSkillRequirements")
 *             array("objects") {
 *                 schema {
 *                     double("weirdNumber")
 *                 }
 *             }
 *         }
 *     }
 * }
 *
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class SchemaDsl

open class BaseArrayConfiguration<T> {
    open var arrayOptions: ArrayFieldOptions<T>? = undefined

    open fun options(block: ArrayFieldOptions<T>.() -> Unit) {
        val opts = ArrayFieldOptions<T>(required = true)
        opts.block()
        arrayOptions = opts
    }
}

@SchemaDsl
class StringArrayConfiguration : BaseArrayConfiguration<String>() {
    var stringOptions: StringFieldOptions? = undefined

    fun string(block: StringFieldOptions.() -> Unit) {
        val opts = StringFieldOptions(required = true)
        opts.block()
        stringOptions = opts
    }
}

@SchemaDsl
class NumberArrayConfiguration<T : Number> : BaseArrayConfiguration<T>() {
    var numberOptions: NumberFieldOptions? = undefined

    fun int(block: NumberFieldOptions.() -> Unit) {
        val opts = NumberFieldOptions(integer = true, required = true)
        opts.block()
        numberOptions = opts
    }

    fun double(block: NumberFieldOptions.() -> Unit) {
        val opts = NumberFieldOptions(required = true)
        opts.block()
        numberOptions = opts
    }
}

@SchemaDsl
class BooleanArrayConfiguration : BaseArrayConfiguration<Boolean>() {
    var booleanOptions: DataFieldOptions/*<Boolean>*/? = undefined

    fun boolean(block: DataFieldOptions/*<Boolean>*/.() -> Unit) {
        val opts = DataFieldOptions/*<Boolean>*/(required = true)
        opts.block()
        booleanOptions = opts
    }
}

@SchemaDsl
class SchemaArrayConfiguration<T> : BaseArrayConfiguration<T>() {
    var schemaOptions: DataFieldOptions? = undefined
    var schema: DataSchema<T>? = null

    fun schemaOptions(block: DataFieldOptions.() -> Unit) {
        val opts = DataFieldOptions(required = true)
        opts.block()
        schemaOptions = opts
    }

    fun schema(block: Schema.() -> Unit) {
        val s = Schema()
        s.block()
        schema = s.build()
    }
}

@SchemaDsl
class Schema {
    val fields = mutableMapOf<String, DataField<out Any>>()

    fun string(
        name: String,
        nullable: Boolean = false,
        context: DataFieldContext<String>? = undefined,
        block: (StringFieldOptions.() -> Unit)? = null,
    ) {
        val options = if (nullable) {
            StringFieldOptions(nullable = true, initial = null, blank = false)
        } else {
            StringFieldOptions(required = true, nullable = false)
        }
        block?.invoke(options)
        fields[name] = StringField(options = options, context = context)
    }

    inline fun <reified T : Enum<T>> enum(
        name: String,
        nullable: Boolean = false,
        context: DataFieldContext<String>? = undefined,
        block: (StringFieldOptions.() -> Unit) = { },
    ) {
        val choices = enumEntries<T>().map { it.toCamelCase() }.toTypedArray()
        val options = if (nullable) {
            StringFieldOptions(nullable = true, initial = null, blank = false, choices = choices)
        } else {
            StringFieldOptions(required = true, nullable = false, choices = choices)
        }
        block.invoke(options)
        fields[name] = StringField(options = options, context = context)
    }

    fun int(
        name: String,
        nullable: Boolean = false,
        context: DataFieldContext<Double>? = undefined,
        block: (NumberFieldOptions.() -> Unit)? = null,
    ) {
        val options = if (nullable) {
            NumberFieldOptions(nullable = true, initial = null, integer = true)
        } else {
            NumberFieldOptions(required = true, nullable = false, integer = true)
        }
        block?.invoke(options)
        fields[name] = NumberField(options = options, context = context)
    }

    fun double(
        name: String,
        nullable: Boolean = false,
        context: DataFieldContext<Double>? = undefined,
        block: (NumberFieldOptions.() -> Unit)? = null,
    ) {
        val options = if (nullable) {
            NumberFieldOptions(nullable = true, initial = null)
        } else {
            NumberFieldOptions(required = true, nullable = false)
        }
        block?.invoke(options)
        fields[name] = NumberField(options = options, context = context)
    }

    fun boolean(
        name: String,
        context: DataFieldContext<Boolean>? = undefined,
        block: (DataFieldOptions/*<Boolean>*/.() -> Unit)? = null,
    ) {
        val options = DataFieldOptions/*<Boolean>*/(required = true, nullable = false)
        block?.invoke(options)
        fields[name] = BooleanField(options = options, context = context)
    }

    fun array(
        name: String,
        context: DataFieldContext<Array<Any>>? = undefined,
        fieldContext: DataFieldContext<Record<String, Any>>? = undefined,
        block: SchemaArrayConfiguration<Any>.() -> Unit,
    ) {
        val opts = SchemaArrayConfiguration<Any>()
        opts.block()
        val element =
            SchemaField(fields = opts.schema ?: recordOf(), options = opts.schemaOptions, context = fieldContext)
        fields[name] =
            ArrayField(element = element, options = opts.arrayOptions, context = context)
    }

    fun stringArray(
        name: String,
        context: DataFieldContext<Array<String>>? = undefined,
        fieldContext: DataFieldContext<String>? = undefined,
        block: (StringArrayConfiguration.() -> Unit)? = null,
    ) {
        val opts = StringArrayConfiguration()
        block?.invoke(opts)
        val element = StringField(options = opts.stringOptions, context = fieldContext)
        fields[name] = ArrayField(element = element, options = opts.arrayOptions, context = context)
    }

    fun <T : Number> numberArray(
        name: String,
        context: DataFieldContext<Array<T>>? = undefined,
        fieldContext: DataFieldContext<T>? = undefined,
        block: (NumberArrayConfiguration<T>.() -> Unit)? = null,
    ) {
        val opts = NumberArrayConfiguration<T>()
        block?.invoke(opts)
        val element = NumberField(options = opts.numberOptions, context = fieldContext)
        fields[name] = ArrayField(element = element, options = opts.arrayOptions, context = context)
    }

    fun booleanArray(
        name: String,
        context: DataFieldContext<Array<Boolean>>? = undefined,
        fieldContext: DataFieldContext<Boolean>? = undefined,
        block: (BooleanArrayConfiguration.() -> Unit)? = null,
    ) {
        val opts = BooleanArrayConfiguration()
        block?.invoke(opts)
        val element = BooleanField(options = opts.booleanOptions, context = fieldContext)
        fields[name] = ArrayField(element = element, options = opts.arrayOptions, context = context)
    }

    fun schema(
        name: String,
        options: DataFieldOptions? = undefined,
        context: DataFieldContext<Record<String, Any>>? = undefined,
        nullable: Boolean = false,
        block: (Schema.() -> Unit)? = null,
    ) {
        val schema = Schema()
        block?.invoke(schema)
        val opts = if (nullable) {
            options?.let { DataFieldOptions.copy(it, nullable = true, initial = null) }
                ?: DataFieldOptions(nullable = true, initial = null)
        } else {
            options?.let { DataFieldOptions.copy(it, required = true, nullable = false) }
                ?: DataFieldOptions(required = true, nullable = false)
        }
        fields[name] = SchemaField(
            fields = schema.build(),
            options = opts,
            context = context,
        )
    }

    fun record(
        name: String,
        context: DataFieldContext<AnyObject>? = undefined,
        block: (DataFieldOptions/*<AnyObject>*/.() -> Unit)? = null,
        nullable: Boolean = false,
    ) {
        val options = if (nullable) {
            DataFieldOptions(nullable = true, initial = null)
        } else {
            DataFieldOptions(required = true, nullable = false)
        }
        block?.invoke(options)
        fields[name] = ObjectField(options = options, context = context)
    }

    fun <T> build(): DataSchema<T> {
        return fields.asSequence()
            .map { it.key to it.value.asDynamic() }
            .toRecord()
    }
}

fun buildSchema(block: Schema.() -> Unit): DataSchema<Any> {
    val schema = Schema()
    schema.block()
    return schema.build()
}