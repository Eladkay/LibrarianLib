package com.teamwizardry.librarianlib.common.util.saving

/**
 * @author WireSegal
 * Created at 3:58 PM on 10/27/16.
 *
 * Apply this to a field to have it be serialized by the write/read nbt methods and write/read byte methods.
 *
 * [saveName] doesn't matter for messages, except in sorting. It's for NBT serializers.
 */
@Target(AnnotationTarget.FIELD)
@MustBeDocumented
annotation class Save(val saveName: String = "")


/**
 * @author WireSegal
 * Created at 5:33 PM on 10/31/16.
 *
 * Apply this to a method to mark it as the getter component for reading/writing a property.
 * A method must exist in the same class with the same [saveName]
 * and with the return type of this function as its single parameter,
 * annotated with [SaveMethodSetter], otherwise nothing will be saved.
 *
 * The "getter" method must take exactly zero parameters, and return the content of the field.
 */
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class SaveMethodGetter(val saveName: String)

/**
 * @author WireSegal
 * Created at 5:38 PM on 10/31/16.
 *
 * Apply this to a method to mark it as the setter component for reading/writing a property.
 * A method must exist in the same class with the same [saveName]
 * and with the input type of this function as its return type,
 * annotated with [SaveMethodGetter], otherwise nothing will be saved.
 *
 * The "setter" method must take exactly one parameter, and its return value will be ignored.
 */
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class SaveMethodSetter(val saveName: String)